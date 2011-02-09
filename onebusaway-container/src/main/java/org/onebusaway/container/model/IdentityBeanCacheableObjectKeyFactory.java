package org.onebusaway.container.model;

import org.onebusaway.container.cache.CacheKeyInfo;
import org.onebusaway.container.cache.CacheableObjectKeyFactory;

/**
 * Provides a {@link CacheableObjectKeyFactory} cache key strategy for objects
 * of type {@link IdentityBean}, where the objects id is used as the cache key.
 * 
 * @author bdferris
 * @see CacheableObjectKeyFactory
 */
public class IdentityBeanCacheableObjectKeyFactory implements
    CacheableObjectKeyFactory {

  public CacheKeyInfo createKey(Object object) {
    return new CacheKeyInfo(((IdentityBean<?>) object).getId(),false);
  }
}
