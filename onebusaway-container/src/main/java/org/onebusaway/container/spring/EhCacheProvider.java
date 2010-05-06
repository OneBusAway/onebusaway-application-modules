package org.onebusaway.container.spring;

import java.util.Properties;

import net.sf.ehcache.CacheManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.CacheProvider;
import org.hibernate.cache.Timestamper;

@SuppressWarnings("deprecation")
public class EhCacheProvider implements CacheProvider {

  private static final Log LOG = LogFactory.getLog(EhCacheProvider.class);

  private CacheManager cacheManager;

  public void setCacheManager(CacheManager cacheManager) {
    this.cacheManager = cacheManager;
  }
  
  @Override
  public Cache buildCache(String name, Properties properties)
      throws CacheException {
    try {
      net.sf.ehcache.Ehcache cache = cacheManager.getEhcache(name);
      if (cache == null) {
        LOG.warn("Could not find a specific ehcache configuration for cache named ["
            + name + "]; using defaults.");
        cacheManager.addCache(name);
        cache = cacheManager.getEhcache(name);
        EhCacheProvider.LOG.debug("started EHCache region: " + name);
      }
      return new net.sf.ehcache.hibernate.EhCache(cache);
    } catch (net.sf.ehcache.CacheException e) {
      throw new CacheException(e);
    }
  }

  @Override
  public boolean isMinimalPutsEnabledByDefault() {
    return false;
  }

  @Override
  public long nextTimestamp() {
    return Timestamper.next();
  }

  @Override
  public void start(Properties properties) throws CacheException {

  }

  @Override
  public void stop() {
    if (cacheManager != null) {
      cacheManager.shutdown();
      cacheManager = null;
    }
  }
}
