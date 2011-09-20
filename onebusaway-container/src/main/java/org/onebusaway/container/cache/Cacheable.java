/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
 * implemented in {@link CacheableAnnotationInterceptor}. The bulk of the caching
 * mechanism is provided by {@link CacheableMethodManager}, which
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
 * @see CacheableAnnotationInterceptor
 * @see CacheableMethodKeyFactory
 * @see CacheableMethodManager
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface Cacheable {

  /**
   * @return a factory class for creating cache keys from method arguments
   */
  Class<? extends CacheableMethodKeyFactory> keyFactory() default CacheableMethodKeyFactory.class;
  
  boolean isValueSerializable() default true;
}
