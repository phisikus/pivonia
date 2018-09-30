package eu.phisikus.pivonia.converter.plaintext;

import dagger.Module;
import dagger.Provides;
import dagger.Reusable;
import eu.phisikus.pivonia.converter.BSONConverter;
import eu.phisikus.pivonia.qualifiers.PlainText;

@Module
public class PlainTextConverterModule {

    @Provides
    @PlainText
    @Reusable
    public BSONConverter provideBSONConverter() {
        return new JacksonBSONConverter();
    }

}
