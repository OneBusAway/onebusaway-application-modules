/*
 * Copyright 2008 Brian Ferris
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
package org.onebusaway.common.spring;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class CacheableInterceptor {

  private CacheManager _cacheManager;

  public void setCacheManager(CacheManager cacheManager) {
    _cacheManager = cacheManager;
  }

  @Around("@annotation(org.onebusaway.common.spring.Cacheable)")
  public Object doBasicProfiling(ProceedingJoinPoint pjp) throws Throwable {

    Cache cache = getCache(pjp);
    String key = getKey(pjp);
    Element element = cache.get(key);

    if (element == null) {
      Object retVal = pjp.proceed();
      element = new Element(key, retVal);
      cache.put(element);
    }

    return element.getValue();
  }

  /***************************************************************************
   * Private Methods
   **************************************************************************/

  private Cache getCache(ProceedingJoinPoint pjp) {
    Signature sig = pjp.getSignature();
    String name = sig.getDeclaringTypeName() + "." + sig.getName();
    Cache cache = _cacheManager.getCache(name);
    if (cache == null) {
      _cacheManager.addCache(name);
      cache = _cacheManager.getCache(name);
    }
    return cache;
  }

  private String getKey(ProceedingJoinPoint pjp) {
    StringBuilder b = new StringBuilder();
    for (Object arg : pjp.getArgs()) {
      if (b.length() > 0)
        b.append(',');
      b.append(arg == null ? "null" : arg.toString());
    }
    return b.toString();
  }
}