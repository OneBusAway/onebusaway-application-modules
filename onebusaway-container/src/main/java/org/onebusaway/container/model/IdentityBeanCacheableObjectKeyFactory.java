package org.onebusaway.container.model;

import org.onebusaway.container.cache.CacheableObjectKeyFactory;

import java.io.Serializable;

/**
 * Provides a {@link CacheableObjectKeyFactory} cache key strategy for objects
 * of type {@link IdentityBean}, where the objects id is used as the cache key.
 * 
 * @author bdferris
 * @see CacheableObjectKeyFactory
 */
public class IdentityBeanCacheableObjectKeyFactory implements
    CacheableObjectKeyFactory {

  public Serializable createKey(Object object) {
    return ((IdentityBean<?>) object).getId();
  }
}
