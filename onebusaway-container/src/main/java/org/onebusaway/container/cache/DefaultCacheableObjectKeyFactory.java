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

  private boolean _cacheRefreshCheck = false;

  public void setCacheRefreshCheck(boolean cacheRefreshCheck) {
    _cacheRefreshCheck = cacheRefreshCheck;
  }

  public boolean isCacheRefreshCheck() {
    return _cacheRefreshCheck;
  }

  public CacheKeyInfo createKey(Object object) {

    if (_cacheRefreshCheck) {
      
      boolean refreshCache = Boolean.TRUE.equals(object);
      
      /**
       * We short-circuit the cache key to Boolean.FALSE, no matter the actual
       * key value so that the resulting cache key will be the same wether a
       * refresh has been requested or not
       */
      return new CacheKeyInfo(Boolean.FALSE, refreshCache);
    }

    if (object instanceof Serializable)
      return new CacheKeyInfo((Serializable) object, false);
    return new CacheKeyInfo(object.toString(), false);
  }
}
