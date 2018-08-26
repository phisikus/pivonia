package eu.phisikus.pivonia.ioc;

import dagger.Component;
import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.api.Server;
import eu.phisikus.pivonia.converter.BSONConverter;

import javax.inject.Singleton;

@Component(modules = PivoniaModule.class)
@Singleton
public interface PivoniaComponent {
    BSONConverter getBSONConverter();

    Client getClient();

    Server getServer();
}
