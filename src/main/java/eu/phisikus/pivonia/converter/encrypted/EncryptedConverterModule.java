package eu.phisikus.pivonia.converter.encrypted;

import dagger.Module;
import dagger.Provides;
import eu.phisikus.pivonia.converter.BSONConverter;
import eu.phisikus.pivonia.crypto.Encryptor;
import eu.phisikus.pivonia.qualifiers.Encrypted;
import eu.phisikus.pivonia.qualifiers.PlainText;

@Module
public class EncryptedConverterModule {
    @Provides
    @Encrypted
    public BSONConverter provideEncryptedBSONConverter(@PlainText BSONConverter bsonConverter, Encryptor encryptor) {
        return new EncryptedBSONConverter(bsonConverter, encryptor);
    }
}
