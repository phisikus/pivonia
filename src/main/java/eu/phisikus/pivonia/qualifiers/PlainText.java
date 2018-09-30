package eu.phisikus.pivonia.qualifiers;

import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * DI qualifier: use plain text implementation
 */
@Qualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface PlainText {
}
