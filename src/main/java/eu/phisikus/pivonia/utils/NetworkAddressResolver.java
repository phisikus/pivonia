package eu.phisikus.pivonia.utils;

import io.vavr.control.Try;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class NetworkAddressResolver {

    private static final String LOCAL_FALLBACK_ADDRESS = "127.0.0.1";

    private List<String> ipProviderUrls = Arrays.asList(
            "http://checkip.amazonaws.com/",
            "https://ipv4.icanhazip.com/",
            "https://myexternalip.com/raw",
            "https://bot.whatismyipaddress.com/"
    );

    private HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    public NetworkAddressResolver() {
    }

    public NetworkAddressResolver(List<String> ipProviderUrls) {
        this.ipProviderUrls = ipProviderUrls;
    }


    public NetworkAddressResolver(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public NetworkAddressResolver(HttpClient httpClient, List<String> ipProviderUrls) {
        this.httpClient = httpClient;
        this.ipProviderUrls = ipProviderUrls;
    }

    /**
     * Discover public IP address of executing machine using external services.
     * @return IP address or empty value if no address could be determined
     */
    public Optional<String> getPublicIp() {
        return ipProviderUrls.stream()
                .map(url -> HttpRequest.newBuilder(URI.create(url)).build())
                .map(httpRequest -> Try.of(() -> httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString())))
                .filter(Try::isSuccess)
                .map(Try::get)
                .map(HttpResponse::body)
                .filter(String::isBlank)
                .findFirst();
    }

    /**
     * Discover local interface IP address of executing machine.
     * @return IP address, local in worst case
     */
    public String getLocalInterfaceAddress() {
        return Try.of(NetworkInterface::networkInterfaces)
                .getOrElse(Stream.empty())
                .filter(nic -> Try.of(() -> !nic.isLoopback() && nic.isUp()).getOrElse(true))
                .flatMap(NetworkInterface::inetAddresses)
                .map(InetAddress::getHostAddress)
                .findFirst()
                .orElse(LOCAL_FALLBACK_ADDRESS);
    }
}
