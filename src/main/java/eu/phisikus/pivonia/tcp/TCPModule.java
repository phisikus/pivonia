package eu.phisikus.pivonia.tcp;


import dagger.Module;
import dagger.Provides;
import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.api.Server;
import eu.phisikus.pivonia.converter.BSONConverter;
import eu.phisikus.pivonia.qualifiers.PlainText;

import javax.inject.Singleton;

@Module
public class TCPModule {
    @Provides
    @Singleton
    public Client provideTCPClient(@PlainText BSONConverter bsonConverter) {
        return new TCPClient(bsonConverter);
    }

    @Provides
    @Singleton
    public Server provideTCPServer(@PlainText BSONConverter bsonConverter) {
        return new TCPServer(bsonConverter);
    }
}
