/*
 * Copyright 2009 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.container.cache;

import java.lang.reflect.Method;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.onebusaway.collections.tuple.T2;
import org.onebusaway.collections.tuple.Tuples;

/**
 * Implements an {@link Aspect} aware interceptor that intercepts calls to
 * methods annotated with {@link Cacheable} and returns a cached result when
 * available or calls the underlying method when not.
 * 
 * @author bdferris
 * @see Cacheable
 */
@Aspect
public class CacheableInterceptor extends AbstractCacheableMethodCallManager {

  @Around("@annotation(org.onebusaway.container.cache.Cacheable)")
  public Object doBasicProfiling(ProceedingJoinPoint pjp) throws Throwable {
    return super.evaluate(pjp);
  }

  @Override
  protected CacheableMethodKeyFactory getKeyFactory(ProceedingJoinPoint pjp) {

    T2<Method, Cacheable> tuple = getCacheableMethodAndAnnotation(pjp);
    if (tuple == null)
      throw new IllegalStateException("no @Cacheable annotation: "
          + pjp.getSignature().toLongString());

    Method m = tuple.getFirst();
    Cacheable c = tuple.getSecond();

    Class<? extends CacheableMethodKeyFactory> keyFactoryType = c.keyFactory();

    if (keyFactoryType.equals(CacheableMethodKeyFactory.class))
      return getCacheableMethodKeyFactoryForMethod(m);

    try {
      return keyFactoryType.newInstance();
    } catch (Exception ex) {
      throw new IllegalStateException(
          "error instantiating CacheableKeyFactory: "
              + keyFactoryType.getName(), ex);
    }
  }

  private T2<Method, Cacheable> getCacheableMethodAndAnnotation(
      ProceedingJoinPoint pjp) {

    List<Method> methods = getMatchingMethodsForJoinPoint(pjp);

    for (Method m : methods) {
      Cacheable c = m.getAnnotation(Cacheable.class);
      if (c != null)
        return Tuples.tuple(m, c);
    }
    return null;
  }
}