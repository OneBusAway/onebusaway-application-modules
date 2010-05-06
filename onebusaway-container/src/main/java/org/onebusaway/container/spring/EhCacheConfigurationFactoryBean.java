package org.onebusaway.container.spring;

import java.io.File;
import java.io.IOException;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;
import net.sf.ehcache.config.DiskStoreConfiguration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

public class EhCacheConfigurationFactoryBean implements FactoryBean,
    InitializingBean {

  protected final Log logger = LogFactory.getLog(getClass());

  private Resource configLocation;

  private Configuration configuration;

  private String diskStorePath;

  public void setConfigLocation(Resource configLocation) {
    this.configLocation = configLocation;
  }

  public void setDiskStorePath(File diskStorePath) {
    this.diskStorePath = diskStorePath.getPath();
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
