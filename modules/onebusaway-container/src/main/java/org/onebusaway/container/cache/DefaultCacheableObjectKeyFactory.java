package org.onebusaway.container.cache;

import java.io.Serializable;

public class DefaultCacheableObjectKeyFactory implements CacheableObjectKeyFactory, Serializable {

  private static final long serialVersionUID = 1L;

  public Serializable createKey(Object object) {
    return object.toString();
  }
}
