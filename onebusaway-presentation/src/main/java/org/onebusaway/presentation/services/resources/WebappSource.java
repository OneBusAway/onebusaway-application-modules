package org.onebusaway.presentation.services.resources;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the classpath location of the resource or resources associated with
 * the ResourcePrototype.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WebappSource {
  String[] value();
}
