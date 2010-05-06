package org.onebusaway.webapp.services.resources;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.gwt.resources.client.ResourcePrototype;

/**
 * Specifies the classpath location of the resource or resources associated with
 * the {@link ResourcePrototype}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WebappSource {
  String[] value();
}
