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
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * Implements an {@link Aspect} aware interceptor that intercepts calls to
 * methods annotated with {@link Cacheable} and returns a cached result when
 * available or calls the underlying method when not.
 * 
 * @author bdferris
 * @see Cacheable
 */
@Aspect
public class CacheableAnnotationInterceptor extends CacheableMethodManager {

  @Around("@annotation(org.onebusaway.container.cache.Cacheable)")
  public Object doBasicProfiling(ProceedingJoinPoint pjp) throws Throwable {
    return evaluate(pjp);
  }
}