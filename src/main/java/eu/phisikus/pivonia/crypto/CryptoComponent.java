package eu.phisikus.pivonia.crypto;

import dagger.Component;

@Component(modules = CryptoModule.class)
public interface CryptoComponent {
    Encryptor getEncryptor();
}
