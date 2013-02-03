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

import java.io.File;
import java.io.IOException;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;
import net.sf.ehcache.config.DiskStoreConfiguration;
import net.sf.ehcache.config.TerracottaClientConfiguration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

/**
 * Spring {@link FactoryBean} for creating an EhCache {@link Configuration} that
 * supports programmatic DiskStore and Terracotta configuration.
 * 
 * A special note about Terracotta config. While you can create all your
 * Terracotta-enabled caches programatically using this FactoryBean, you need to
 * have at least one Terracotta-enabled cache created the old fashioned way
 * (through an {@code ehcache.xml} resource config) so that the
 * {@link CacheManager} will properly enable Terracotta support.
 * 
 * @author bdferris
 * @see EhCacheFactoryBean
 * @see EhCacheManagerFactoryBean
 */
public class EhCacheConfigurationFactoryBean implements FactoryBean<Configuration> ,
    InitializingBean {

  protected final Log logger = LogFactory.getLog(getClass());

  private Resource configLocation;

  private Configuration configuration;

  private String diskStorePath;

  private String terracottaUrl;

  public void setConfigLocation(Resource configLocation) {
    this.configLocation = configLocation;
  }

  public void setDiskStorePath(File diskStorePath) {
    this.diskStorePath = diskStorePath.getPath();
  }

  public void setTerracottaUrl(String terracottaUrl) {
    this.terracottaUrl = terracottaUrl;
  }

  public void afterPropertiesSet() throws IOException, CacheException {
    logger.info("Initializing EHCache CacheManager");
    this.configuration = ConfigurationFactory.parseConfiguration(this.configLocation.getInputStream());
    if (this.diskStorePath != null) {
      logger.info("diskStorePath=" + this.diskStorePath);
      DiskStoreConfiguration dsConfig = new DiskStoreConfiguration();
      dsConfig.setPath(this.diskStorePath);
      logger.info("diskStorePath (translated)=" + dsConfig.getPath());
      configuration.addDiskStore(dsConfig);
    }
    if (this.terracottaUrl != null) {
      logger.info("terracottaUrl=" + this.terracottaUrl);
      TerracottaClientConfiguration tcConfig = new TerracottaClientConfiguration();
      tcConfig.setUrl(this.terracottaUrl);
      configuration.addTerracottaConfig(tcConfig);
    }
  }

  public Configuration getObject() {
    return this.configuration;
  }

  public Class<?> getObjectType() {
    return (this.configuration != null ? this.configuration.getClass()
        : Configuration.class);
  }

  public boolean isSingleton() {
    return true;
  }
}
