package org.onebusaway.gtfs.impl;

import org.onebusaway.container.cache.CacheKeyInfo;
import org.onebusaway.container.cache.CacheableObjectKeyFactory;
import org.onebusaway.gtfs.model.IdentityBean;

public class IdentityBeanCacheableObjectKeyFactory implements
    CacheableObjectKeyFactory {

  public CacheKeyInfo createKey(Object object) {
    return new CacheKeyInfo(((IdentityBean<?>) object).getId(), false);
  }
}
