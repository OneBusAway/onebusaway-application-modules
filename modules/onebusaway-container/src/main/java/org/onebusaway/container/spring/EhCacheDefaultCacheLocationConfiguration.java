package org.onebusaway.container.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.util.Properties;

public class EhCacheDefaultCacheLocationConfiguration {

  private final Logger _log = LoggerFactory.getLogger(EhCacheDefaultCacheLocationConfiguration.class);

  private static final String DISK_STORE_PROPERTY = "ehcache.disk.store.dir";

  private ApplicationContext _context;

  @Autowired
  public void setApplicationContext(ApplicationContext context) {
    _context = context;
  }

  public void init() {

    Properties properties = System.getProperties();

    if (properties.containsKey(DISK_STORE_PROPERTY))
      return;

    if (_context.containsBean("bundlePath")) {
      File path = (File) _context.getBean("bundlePath");
      _log.info("Setting " + DISK_STORE_PROPERTY + "=" + path.getAbsolutePath());
      properties.setProperty(DISK_STORE_PROPERTY, path.getAbsolutePath());
    } else {
      String defaultLocation = properties.getProperty("java.io.tmpdir");
      _log.info("Setting default " + DISK_STORE_PROPERTY + "="
          + defaultLocation);
      properties.setProperty(DISK_STORE_PROPERTY, defaultLocation);
    }
  }
}
