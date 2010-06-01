package org.onebusaway.container.cache;

import java.io.Serializable;

/**
 * Factory for producing a {@link Serializable} cache key from a and arbitrary
 * object. This default implementation just uses the {@link #toString()}
 * representation of the object as the cache key.
 * 
 * @author bdferris
 * @see CacheableObjectKeyFactory
 * @see DefaultCacheableKeyFactory
 */
public class DefaultCacheableObjectKeyFactory implements
    CacheableObjectKeyFactory, Serializable {

  private static final long serialVersionUID = 1L;

  public Serializable createKey(Object object) {
    return object.toString();
  }
}
