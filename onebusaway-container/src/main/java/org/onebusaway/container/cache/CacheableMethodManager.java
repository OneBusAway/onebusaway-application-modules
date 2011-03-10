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

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;

/**
 * Support class providing functionality for caching the output of arbitrary
 * method calls, using the arguments to the method to generate the cache key.
 * 
 * EhCache is used as the backing cache store.
 * 
 * @author bdferris
 * @see Cacheable
 * @see CacheableAnnotationInterceptor
 * @see CacheableMethodKeyFactory
 * @see CacheableMethodKeyFactoryManager
 */
public class CacheableMethodManager {

  private ConcurrentHashMap<String, CacheEntry> _entries = new ConcurrentHashMap<String, CacheEntry>();

  private CacheManager _cacheManager;

  protected CacheableMethodKeyFactoryManager _cacheableMethodKeyFactoryManager;

  private String _cacheNamePrefix;

  public void setCacheManager(CacheManager cacheManager) {
    _cacheManager = cacheManager;
  }

  public void setCacheableMethodKeyFactoryManager(
      CacheableMethodKeyFactoryManager cacheableMethodKeyFactoryManager) {
    _cacheableMethodKeyFactoryManager = cacheableMethodKeyFactoryManager;
  }

  public void setCacheNamePrefix(String cacheNamePrefix) {
    _cacheNamePrefix = cacheNamePrefix;
  }

  public Object evaluate(ProceedingJoinPoint pjp) throws Throwable {

    CacheEntry entry = getCache(pjp);
    CacheableMethodKeyFactory keyFactory = entry.getKeyFactory();
    Cache cache = entry.getCache();
    CacheKeyInfo keyInfo = keyFactory.createKey(pjp);
    Serializable key = keyInfo.getKey();

    Element element = cache.get(key);

    if (element == null || keyInfo.isCacheRefreshIndicated()) {
      Object retVal = pjp.proceed();
      element = new Element(key, retVal);
      cache.put(element);
    }

    return element.getValue();
  }

  /***************************************************************************
   * Protected Methods
   **************************************************************************/

  protected CacheableMethodKeyFactory getKeyFactory(ProceedingJoinPoint pjp) {
    return _cacheableMethodKeyFactoryManager.getCacheableMethodKeyFactoryForJoinPoint(pjp);
  }

  protected String getCacheName(ProceedingJoinPoint pjp) {
    Signature sig = pjp.getSignature();
    StringBuilder b = new StringBuilder();
    if (_cacheNamePrefix != null)
      b.append(_cacheNamePrefix).append("-");
    b.append(sig.getDeclaringTypeName()).append('.').append(sig.getName());
    return b.toString();
  }

  protected Cache createCache(ProceedingJoinPoint pjp, String name) {
    return null;
  }

  /****
   * Private Methods
   ****/

  private CacheEntry getCache(ProceedingJoinPoint pjp) {

    String name = getCacheName(pjp);

    CacheEntry entry = _entries.get(name);

    if (entry == null) {
      CacheableMethodKeyFactory keyFactory = getKeyFactory(pjp);
      Cache cache = _cacheManager.getCache(name);
      if (cache == null) {
        cache = createCache(pjp, name);
        if (cache == null) {
          _cacheManager.addCache(name);
          cache = _cacheManager.getCache(name);
        } else {
          _cacheManager.addCache(cache);
        }
      }
      entry = new CacheEntry(keyFactory, cache);
      _entries.put(name, entry);
    }
    return entry;
  }

  private static class CacheEntry {

    private CacheableMethodKeyFactory _keyFactory;

    private Cache _cache;

    public CacheEntry(CacheableMethodKeyFactory keyFactory, Cache cache) {
      _keyFactory = keyFactory;
      _cache = cache;
    }

    public CacheableMethodKeyFactory getKeyFactory() {
      return _keyFactory;
    }

    public Cache getCache() {
      return _cache;
    }
  }
}