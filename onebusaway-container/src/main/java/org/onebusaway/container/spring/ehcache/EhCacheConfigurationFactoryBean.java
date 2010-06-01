package org.onebusaway.container.spring.ehcache;

import java.io.File;
import java.io.IOException;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;
import net.sf.ehcache.config.DiskStoreConfiguration;
import net.sf.ehcache.config.TerracottaConfigConfiguration;

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
public class EhCacheConfigurationFactoryBean implements FactoryBean,
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
      TerracottaConfigConfiguration tcConfig = new TerracottaConfigConfiguration();
      tcConfig.setUrl(this.terracottaUrl);
      configuration.addTerracottaConfig(tcConfig);
    }
  }

  public Object getObject() {
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
