package eu.phisikus.pivonia.tcp.utils;

import eu.phisikus.pivonia.pool.address.Address;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * Provides public network address using external service.
 * Available port is chosen from local opened ports.
 */
@NoArgsConstructor
@AllArgsConstructor
public class PublicAddressResolver implements NetworkAddressResolver {

    private final Predicate<HttpResponse<String>> isPositiveResponse = response -> {
        var statusCode = response.statusCode();
        return statusCode >= 200 && statusCode < 300;
    };

    private List<String> ipProviderUrls = Arrays.asList(
            "http://checkip.amazonaws.com/",
            "https://ipv4.icanhazip.com/",
            "https://myexternalip.com/raw",
            "https://bot.whatismyipaddress.com/"
    );

    private HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NEVER)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public PublicAddressResolver(List<String> ipProviderUrls) {
        this.ipProviderUrls = ipProviderUrls;
    }

    public PublicAddressResolver(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public Try<Address> getAddress() {
        return getHost()
                .flatMap(address -> AvailablePortProvider
                        .getRandomPort()
                        .map(port -> new Address(address, port))
                ).recoverWith(throwable -> Try.failure(new AddressNotResolvedException(throwable)));
    }

    private Try<String> getHost() {
        return ipProviderUrls.stream()
                .map(url -> HttpRequest.newBuilder(URI.create(url)).build())
                .map(httpRequest -> Try.of(() -> httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString())))
                .filter(Try::isSuccess)
                .map(Try::get)
                .filter(isPositiveResponse)
                .map(HttpResponse::body)
                .map(String::strip)
                .filter(address -> !address.isBlank())
                .findFirst()
                .map(Try::success)
                .orElseGet(() -> Try.failure(new AddressNotResolvedException()));
    }
}
