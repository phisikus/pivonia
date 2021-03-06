package eu.phisikus.pivonia.tcp;


import dagger.Module;
import dagger.Provides;
import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.api.Server;
import eu.phisikus.pivonia.converter.BSONConverter;
import eu.phisikus.pivonia.qualifiers.Encrypted;
import eu.phisikus.pivonia.qualifiers.PlainText;

@Module
public class TCPModule {
    @Provides
    public Client provideTCPClient(@PlainText BSONConverter bsonConverter) {
        return new TCPClient(bsonConverter);
    }

    @Provides
    public Server provideTCPServer(@PlainText BSONConverter bsonConverter) {
        return new TCPServer(bsonConverter);
    }

    @Provides
    @Encrypted
    public Client provideTCPClientWithEncryption(@Encrypted BSONConverter bsonConverter) {
        return new TCPClient(bsonConverter);
    }

    @Provides
    @Encrypted
    public Server provideTCPServerWithEncryption(@Encrypted BSONConverter bsonConverter) {
        return new TCPServer(bsonConverter);
    }
}
