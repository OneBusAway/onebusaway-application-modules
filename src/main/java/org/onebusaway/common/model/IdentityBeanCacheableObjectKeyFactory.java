package org.onebusaway.common.model;

import org.onebusaway.common.spring.CacheableObjectKeyFactory;

import java.io.Serializable;

public class IdentityBeanCacheableObjectKeyFactory implements CacheableObjectKeyFactory {

  public Serializable createKey(Object object) {
    return ((IdentityBean<?>) object).getId();
  }
}
