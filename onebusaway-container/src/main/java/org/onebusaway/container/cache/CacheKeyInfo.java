package org.onebusaway.container.cache;

import java.io.Serializable;

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
