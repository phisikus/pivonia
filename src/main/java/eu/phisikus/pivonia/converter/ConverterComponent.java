package eu.phisikus.pivonia.converter;

import dagger.Component;

@Component(modules = ConverterModule.class)
public interface ConverterComponent {
    BSONConverter getBSONConverter();
}
