package org.onebusaway.container.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.File;
import java.util.Properties;

public class EhCacheDefaultCacheLocationConfigurationFactory implements ApplicationContextAware {

  private final Logger _log = LoggerFactory.getLogger(EhCacheDefaultCacheLocationConfigurationFactory.class);

  private static final String DISK_STORE_PROPERTY = "ehcache.disk.store.dir";

  private ApplicationContext _context;

  @Autowired
  public void setApplicationContext(ApplicationContext context) {
    _context = context;
  }

  public Object init() {

    Properties properties = System.getProperties();

    if (!properties.containsKey(DISK_STORE_PROPERTY)) {

      if (_context != null && _context.containsBean("bundlePath")) {
        File path = (File) _context.getBean("bundlePath");
        _log.info("Setting " + DISK_STORE_PROPERTY + "="
            + path.getAbsolutePath());
        properties.setProperty(DISK_STORE_PROPERTY, path.getAbsolutePath());
      } else {
        String defaultLocation = properties.getProperty("java.io.tmpdir");
        _log.info("Setting default " + DISK_STORE_PROPERTY + "="
            + defaultLocation);
        properties.setProperty(DISK_STORE_PROPERTY, defaultLocation);
      }
    }

    return properties.getProperty(DISK_STORE_PROPERTY);
  }
}
