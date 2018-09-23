package eu.phisikus.pivonia.converter;

import dagger.Module;
import dagger.Provides;
import dagger.Reusable;

@Module
public class ConverterModule {

    @Provides
    @Reusable
    public BSONConverter provideBSONConverter() {
        return new JacksonBSONConverter();
    }
}
