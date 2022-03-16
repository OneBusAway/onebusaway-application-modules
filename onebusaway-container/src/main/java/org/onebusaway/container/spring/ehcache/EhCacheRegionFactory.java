/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.container.spring.ehcache;

import java.util.Map;
import java.util.Properties;

import net.sf.ehcache.CacheManager;

import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.cache.CacheException;

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
public class EhCacheRegionFactory extends org.hibernate.cache.ehcache.EhCacheRegionFactory {

  private static CacheManager staticCacheManagerInstance;

  public static void setStaticCacheManagerInstance(CacheManager cacheManager) {
    staticCacheManagerInstance = cacheManager;
  }

  public EhCacheRegionFactory(Properties prop) {
    super(prop);
  }

  @Override
  public void start(SessionFactoryOptions settings, Map<String, Object> configValues)
      throws CacheException {
    if (staticCacheManagerInstance != null)
      manager = staticCacheManagerInstance;
    else
      super.start(settings, configValues);
  }

  @Override
  public void stop() {
    if (staticCacheManagerInstance == null)
      super.stop();
  }
}
