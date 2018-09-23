package eu.phisikus.pivonia.tcp;

import dagger.Component;
import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.api.Server;
import eu.phisikus.pivonia.converter.ConverterComponent;

import javax.inject.Singleton;

@Component(
        modules = TCPModule.class,
        dependencies = ConverterComponent.class
)
@Singleton
public interface TCPComponent {
    Client getClient();

    Server getServer();
}
