package org.onebusaway.container.cache;

import java.io.Serializable;

/**
 * Encapsulates cache key information, including the cache key for a particular
 * method call and whether the cache should be refreshed for a particular method
 * call.
 * 
 * @author bdferris
 * @see CacheableMethodKeyFactory
 * @see CacheableObjectKeyFactory 
 */
public final class CacheKeyInfo implements Serializable {

  private static final long serialVersionUID = 1L;

  private final Serializable key;

  private final boolean cacheRefreshIndicated;

  public CacheKeyInfo(Serializable key, boolean cacheRefreshIndicated) {
    this.key = key;
    this.cacheRefreshIndicated = cacheRefreshIndicated;
  }

  public Serializable getKey() {
    return key;
  }

  public boolean isCacheRefreshIndicated() {
    return cacheRefreshIndicated;
  }

}
