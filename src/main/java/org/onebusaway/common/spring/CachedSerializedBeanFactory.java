package org.onebusaway.common.spring;


import org.onebusaway.common.impl.ObjectSerializationLibrary;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CachedSerializedBeanFactory implements FactoryBean {

  private static Logger _logger = Logger.getLogger(CachedSerializedBeanFactory.class.getName());

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
        _logger.log(Level.WARNING, "error reading serialized bean from file "
            + _file, ex);
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
