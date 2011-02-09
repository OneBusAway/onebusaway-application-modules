package org.onebusaway.container.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Type annotation indicating how a particular object should be used to generate
 * a cache key when used as an argument to a method annotated with
 * {@link Cacheable}.
 * 
 * @author bdferris
 * @see Cacheable
 * @see CacheableObjectKeyFactory
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
public @interface CacheableKey {
  Class<? extends CacheableObjectKeyFactory> keyFactory();
}
