package org.onebusaway.container.cache;

import org.onebusaway.utility.ObjectSerializationLibrary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.io.File;

public class CachedSerializedBeanFactory implements FactoryBean {

  private final Logger _logger = LoggerFactory.getLogger(CachedSerializedBeanFactory.class);

  private ApplicationContext _context;

  private String _beanName;

  private File _file;

  private boolean _singleton = false;

  private Object _instance = null;

  @Autowired
  public void setApplicationContext(ApplicationContext context) {
    _context = context;
  }

  public void setBeanName(String beanName) {
    _beanName = beanName;
  }

  public void setFile(File file) {
    _file = file;
  }

  public void setSingleton(boolean singleton) {
    _singleton = singleton;
  }

  public Class<?> getObjectType() {
    return null;
  }

  public boolean isSingleton() {
    return _singleton;
  }

  public Object getObject() throws Exception {

    if (_singleton && _instance != null)
      return _instance;

    if (_file.exists()) {
      try {
        Object obj = ObjectSerializationLibrary.readObject(_file);
        if (_singleton)
          _instance = obj;
        return obj;
      } catch (Exception ex) {
        _logger.warn("error reading serialized bean from file " + _file, ex);
      }
    }

    System.out.println("generating bean: " + _beanName);
    Object obj = _context.getBean(_beanName);
    ObjectSerializationLibrary.writeObject(_file, obj);
    if (_singleton)
      _instance = obj;

    return obj;
  }
}
