package org.onebusaway.container.cache;

import java.io.Serializable;

/**
 * Factory for producing a {@link Serializable} cache key from a and arbitrary
 * object.
 * 
 * @author bdferris
 * @see DefaultCacheableKeyFactory
 * @see DefaultCacheableObjectKeyFactory
 */
public interface CacheableObjectKeyFactory {
  public Serializable createKey(Object object);
}
