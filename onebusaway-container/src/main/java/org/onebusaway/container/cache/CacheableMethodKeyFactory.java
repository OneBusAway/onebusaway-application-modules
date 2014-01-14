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

import org.aspectj.lang.ProceedingJoinPoint;

import java.io.Serializable;

/**
 * Factory for producing a {@link Serializable} cache key from a
 * {@link ProceedingJoinPoint} method invocation. Typically, the arguments to
 * the method are used to generate the cache key, though the details are left up
 * to the implementor.
 * 
 * @author bdferris
 * @see Cacheable#keyFactory()
 * @see DefaultCacheableKeyFactory
 */
public interface CacheableMethodKeyFactory {
  public CacheKeyInfo createKey(ProceedingJoinPoint point);
}
