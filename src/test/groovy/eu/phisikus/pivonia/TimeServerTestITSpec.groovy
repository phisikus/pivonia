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
import spock.lang.Specification

class TimeServerTestITSpec extends Specification {

    class TimeLayer implements Middleware<TimeMessage> {
        @Override
        void initialize(MiddlewareClient<TimeMessage> middlewareClient) throws MissingMiddlewareException {
        }

        @Override
        Optional<TimeMessage> handleIncomingMessage(TimeMessage message) {
            return Optional.of(new TimeMessage(,message.getRe))
        }

        @Override
        Optional<TimeMessage> handleOutgoingMessage(TimeMessage message) {
            return null
        }

        @Override
        void close() throws Exception {

        }
    }

    void setupSpec() {

        final encryptionKey = UUID.randomUUID().toString().getBytes()

        final cryptoComponent = DaggerCryptoComponent.builder()
                .cryptoModule(new CryptoModule(encryptionKey))
                .build()

        final converterComponent = DaggerConverterComponent.builder()
                .cryptoComponent(cryptoComponent)
                .build()

        final tcpComponent = DaggerTCPComponent.builder()
                .converterComponent(converterComponent)
                .build()

        def timeServerCake = new CakeWithClientPool(TimeMessage)

        def serverPort = ServerTestUtils.getRandomPort()

        def server = tcpComponent.getServerWithEncryption()
        timeServerCake.getClientPool().addSourceUsingBuilder({
            handler -> server.bind(serverPort, handler)
        })
        timeServerCake.addLayer(new IdLayer("server1"))
        timeServerCake.initialize()
    }
}
