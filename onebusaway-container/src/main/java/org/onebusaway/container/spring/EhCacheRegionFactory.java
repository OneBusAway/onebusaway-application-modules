package org.onebusaway.container.spring;

import java.util.Properties;

import net.sf.ehcache.CacheManager;

import org.hibernate.cache.CacheException;
import org.hibernate.cfg.Settings;

public class EhCacheRegionFactory extends
    net.sf.ehcache.hibernate.EhCacheRegionFactory {

  private static CacheManager staticCacheManagerInstance;

  public static void setStaticCacheManagerInstance(CacheManager cacheManager) {
    staticCacheManagerInstance = cacheManager;
  }

  public EhCacheRegionFactory(Properties prop) {
    super(prop);
  }

  @Override
  public void start(Settings settings, Properties properties)
      throws CacheException {
    if (staticCacheManagerInstance != null)
      manager = staticCacheManagerInstance;
    else
      super.start(settings, properties);
  }

  @Override
  public void stop() {
    if (staticCacheManagerInstance == null)
      super.stop();
  }
}
