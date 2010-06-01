package org.onebusaway.container.spring.ehcache;

import java.util.Properties;

import net.sf.ehcache.CacheManager;

import org.hibernate.cache.CacheException;
import org.hibernate.cfg.Settings;

/**
 * A Hibernate EhCacheRegionFactory implementation that supports directly
 * setting the {@link CacheManager} from an existing instance. This allows us to
 * dynamically configure out CacheManager using Spring and then pass it into
 * Hibernate.
 * 
 * Unfortunately, the EhCacheRegionFactory cannot be passed as an instance
 * object to Hibernate, but instead as a class name to be created by Hibernate
 * itself. Thus, there is no easy way to connect it the Spring application
 * context. As a kludge to get around that, we have a static
 * {@link #setStaticCacheManagerInstance(CacheManager)} that can be called from
 * the Spring application context config to supply the {@link CacheManager}
 * instance. It's a hack, but I don't see any way around it.
 * 
 * @author bdferris
 * 
 */
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
