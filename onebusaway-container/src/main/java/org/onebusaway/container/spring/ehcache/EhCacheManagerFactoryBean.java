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

import java.io.IOException;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.Configuration;

import net.sf.ehcache.config.ConfigurationFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

/**
 * * A Spring {@link FactoryBean} for programmatically creating an EhCache
 * {@link CacheManager}, specifically allowing us to pass in a
 * {@link Configuration} directly. The existing factory bean implementation
 * provided by Spring directly does not allow one to programmatically specify
 * the {@link Configuration} directly, which makes it difficult to dynamically
 * configure the cache using a spring application context config.
 * 
 * @author bdferris
 * @see EhCacheConfigurationFactoryBean
 * @see EhCacheFactoryBean
 */
public class EhCacheManagerFactoryBean implements FactoryBean<CacheManager>,
    InitializingBean, DisposableBean {

  protected final Log logger = LogFactory.getLog(getClass());

  private Resource configLocation;

  private Configuration configuration;

  private String cacheManagerName;

  private CacheManager cacheManager;

  public void setConfigLocation(Resource configLocation) {
    this.configLocation = configLocation;
  }

  public void setConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }

  /**
   * Set the name of the EHCache CacheManager (if a specific name is desired).
   * 
   * @see net.sf.ehcache.CacheManager#setName(String)
   */
  public void setCacheManagerName(String cacheManagerName) {
    this.cacheManagerName = cacheManagerName;
  }

  public void afterPropertiesSet() throws IOException, CacheException {
    logger.info("Initializing EHCache CacheManager");

    // Independent CacheManager instance (the default).
    Configuration config;
    if (this.configuration != null) {
      config = this.configuration;
    } else if (this.configLocation != null) {
      config = ConfigurationFactory.parseConfiguration(this.configLocation.getInputStream());
    } else {
      config = new Configuration();
    }

    if (this.cacheManagerName != null) {
      config.setName(this.cacheManagerName);
    }

    this.cacheManager = new CacheManager(config);

    if (!this.cacheManager.isNamed()) {
      logger.error("cacheManager is not named, this may cause problems.  Please set cacheManagerName in your spring configuration");
    }
  }

  public CacheManager getObject() {
    return this.cacheManager;
  }

  public Class<?> getObjectType() {
    return (this.cacheManager != null ? this.cacheManager.getClass()
        : CacheManager.class);
  }

  public boolean isSingleton() {
    return true;
  }

  public void destroy() {
    logger.info("Shutting down EHCache CacheManager");
    this.cacheManager.shutdown();
  }

}
