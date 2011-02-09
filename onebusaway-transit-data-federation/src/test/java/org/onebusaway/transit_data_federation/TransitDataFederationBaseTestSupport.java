package org.onebusaway.transit_data_federation;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

public abstract class TransitDataFederationBaseTestSupport {

  public static Cache createCache() {
    CacheManager manager = new CacheManager(
        TransitDataFederationBaseTestSupport.class.getResourceAsStream("ehcache.xml"));
    manager.addCache("cache");
    return manager.getCache("cache");
  }
}
