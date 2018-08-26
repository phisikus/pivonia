package eu.phisikus.pivonia.ioc;


import dagger.Module;
import dagger.Provides;
import dagger.Reusable;
import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.api.Server;
import eu.phisikus.pivonia.converter.BSONConverter;
import eu.phisikus.pivonia.converter.JacksonBSONConverter;
import eu.phisikus.pivonia.tcp.TCPClient;
import eu.phisikus.pivonia.tcp.TCPServer;

import javax.inject.Singleton;

@Module
public class PivoniaModule {
    @Provides
    @Reusable
    public BSONConverter provideConverter() {
        return new JacksonBSONConverter();
    }

    @Provides
    @Singleton
    public Client provideTCPClient(BSONConverter bsonConverter) {
        return new TCPClient(bsonConverter);
    }

    @Provides
    @Singleton
    public Server provideTCPServer(BSONConverter bsonConverter) {
        return new TCPServer(bsonConverter);
    }
}
