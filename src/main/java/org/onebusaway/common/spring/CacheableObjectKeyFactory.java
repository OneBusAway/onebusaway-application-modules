package org.onebusaway.common.spring;

import java.io.Serializable;

public interface CacheableObjectKeyFactory {
  public Serializable createKey(Object object);
}
