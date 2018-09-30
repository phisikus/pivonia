package eu.phisikus.pivonia.crypto;

import dagger.Module;
import dagger.Provides;

@Module
public class CryptoModule {

    private byte[] encryptionKey;

    public CryptoModule(byte[] encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    @Provides
    Encryptor provideEncryptor() {
        return new SymmetricalEncryptor(encryptionKey);
    }
}
