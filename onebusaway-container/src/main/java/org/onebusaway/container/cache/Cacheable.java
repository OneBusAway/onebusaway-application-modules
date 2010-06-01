package org.onebusaway.container.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A method annotation that indicates that the results method are cacheable,
 * using the arguments to the method as the cache key.
 * 
 * Method calls are intercepted using an Aspect-oriented interceptor, as
 * implemented in {@link CacheableInterceptor}. The bulk of the caching
 * mechanism is provided by {@link AbstractCacheableMethodCallManager}, which
 * handles the actual task of generating a cache key for a particular method
 * call, determining if a cached results exists, and generating a fresh result
 * as needed by completing the underlying method call. EhCache is used as the
 * default caching mechanism. Unless otherwise specified with {@link #name()},
 * each method cache name takes the form of "package.SomeClass.methodName". Use
 * standard EhCache configuration to provide specific cache behavior for a
 * particular cached method call.
 * 
 * The {@link #keyFactory()} method allows an extension mechanism to define a
 * {@link CacheableMethodKeyFactory} that determines how the arguments to a
 * particular method will be used to generate a cache key. By default, the
 * {@link DefaultCacheableKeyFactory} factory is used, which simply uses
 * {@link DefaultCacheableObjectKeyFactory} factories to generate a key for each
 * argument, unless otherwise specified with a {@link CacheableKey} annotation.
 * 
 * @author bdferris
 * @see CacheableInterceptor
 * @see CacheableMethodKeyFactory
 * @see AbstractCacheableMethodCallManager
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface Cacheable {

  /**
   * @return the name of the cache to use. Defaults to
   *         "package.Class.methodName"
   */
  String name() default "";

  /**
   * 
   * 
   * @return a factory class for creating cache keys from method arguments
   */
  Class<? extends CacheableMethodKeyFactory> keyFactory() default CacheableMethodKeyFactory.class;
}
