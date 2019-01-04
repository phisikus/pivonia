package eu.phisikus.pivonia

import eu.phisikus.pivonia.api.middleware.Middleware
import eu.phisikus.pivonia.api.middleware.MiddlewareClient
import eu.phisikus.pivonia.converter.DaggerConverterComponent
import eu.phisikus.pivonia.crypto.CryptoModule
import eu.phisikus.pivonia.crypto.DaggerCryptoComponent
import eu.phisikus.pivonia.middleware.CakeWithClientPool
import eu.phisikus.pivonia.middleware.MissingMiddlewareException
import eu.phisikus.pivonia.middleware.layer.IdLayer
import eu.phisikus.pivonia.tcp.DaggerTCPComponent
import eu.phisikus.pivonia.tcp.TCPComponent
import eu.phisikus.pivonia.test.CryptoUtils
import eu.phisikus.pivonia.test.ServerTestUtils
import io.vavr.NotImplementedError
import org.apache.tools.ant.util.FileUtils
import spock.lang.Shared
import spock.lang.Specification

import java.time.Instant

class TimeServerTestITSpec extends Specification {

    @Shared
    def testKeyFilename

    final serverId = "server1"

    private class TimeLayer implements Middleware<TimeMessage> {
        @Override
        void initialize(MiddlewareClient<TimeMessage> middlewareClient) throws MissingMiddlewareException {
        }

        @Override
        Optional<TimeMessage> handleIncomingMessage(TimeMessage message) {
            def timeMessage = new TimeMessage(
                    message.getRecipientId(),
                    message.getSenderId(),
                    Instant.now().toEpochMilli()
            )
            return Optional.of(timeMessage)
        }

        @Override
        Optional<TimeMessage> handleOutgoingMessage(TimeMessage message) {
            throw new NotImplementedError()
        }

        @Override
        void close() throws Exception {
        }
    }

    def "Should test time server"() {
        // TODO add real test + refactor
        expect:
        true

    }

    void cleanupSpec() {
        FileUtils.delete(new File(testKeyFilename))
    }


    void setupSpec() {

        TCPComponent tcpComponent = buildIoCDependencies()

        buildCake(tcpComponent)
    }

    private void buildCake(TCPComponent tcpComponent) {
        def timeServerCake = new CakeWithClientPool(TimeMessage)
        def server = tcpComponent.getServerWithEncryption()
        timeServerCake.getClientPool().addSourceUsingBuilder({
            handler -> server.bind(ServerTestUtils.getRandomPort(), handler)
        })
        timeServerCake.addLayer(new IdLayer(serverId))
        timeServerCake.initialize()
    }

    private TCPComponent buildIoCDependencies() {
        testKeyFilename = CryptoUtils.buildRandomKeyset()
        def testKeyContent = CryptoUtils.getKeysetContent(testKeyFilename)

        final cryptoComponent = DaggerCryptoComponent.builder()
                .cryptoModule(new CryptoModule(testKeyContent))
                .build()

        final converterComponent = DaggerConverterComponent.builder()
                .cryptoComponent(cryptoComponent)
                .build()

        return DaggerTCPComponent.builder()
                .converterComponent(converterComponent)
                .build()
    }
}
