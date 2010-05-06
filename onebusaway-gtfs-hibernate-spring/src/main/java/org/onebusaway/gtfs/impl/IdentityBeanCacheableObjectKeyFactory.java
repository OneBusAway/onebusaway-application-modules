package org.onebusaway.gtfs.impl;

import org.onebusaway.container.cache.CacheableObjectKeyFactory;
import org.onebusaway.gtfs.model.IdentityBean;

import java.io.Serializable;

public class IdentityBeanCacheableObjectKeyFactory implements
    CacheableObjectKeyFactory {

  public Serializable createKey(Object object) {
    return ((IdentityBean<?>) object).getId();
  }
}
