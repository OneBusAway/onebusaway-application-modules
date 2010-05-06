package org.onebusaway.container.cache;

import java.io.Serializable;

public interface CacheableObjectKeyFactory {
  public Serializable createKey(Object object);
}
