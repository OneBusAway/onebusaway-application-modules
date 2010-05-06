package org.onebusaway.container.model;

import org.onebusaway.container.cache.CacheableObjectKeyFactory;

import java.io.Serializable;

public class IdentityBeanCacheableObjectKeyFactory implements CacheableObjectKeyFactory {

  public Serializable createKey(Object object) {
    return ((IdentityBean<?>) object).getId();
  }
}
