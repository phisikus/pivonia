package eu.phisikus.pivonia.tcp;

import dagger.Component;
import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.api.Server;
import eu.phisikus.pivonia.converter.ConverterComponent;
import eu.phisikus.pivonia.qualifiers.Encrypted;

@Component(
        modules = TCPModule.class,
        dependencies = ConverterComponent.class
)
public interface TCPComponent {
    Client getClient();

    Server getServer();

    @Encrypted
    Client getClientWithEncryption();

    @Encrypted
    Server getServerWithEncryption();
}
