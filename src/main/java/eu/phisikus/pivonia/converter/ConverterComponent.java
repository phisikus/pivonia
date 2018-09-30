package eu.phisikus.pivonia.converter;

import dagger.Component;
import eu.phisikus.pivonia.converter.encrypted.EncryptedConverterModule;
import eu.phisikus.pivonia.converter.plaintext.PlainTextConverterModule;
import eu.phisikus.pivonia.crypto.CryptoComponent;
import eu.phisikus.pivonia.qualifiers.Encrypted;
import eu.phisikus.pivonia.qualifiers.PlainText;

@Component(
        modules = {
                PlainTextConverterModule.class,
                EncryptedConverterModule.class},
        dependencies = {
                CryptoComponent.class
        })
public interface ConverterComponent {
    @PlainText
    BSONConverter getBSONConverter();

    @Encrypted
    BSONConverter getBSONConverterWithEncryption();
}
