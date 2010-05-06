package org.onebusaway.common.spring;

import org.onebusaway.common.impl.ObjectSerializationLibrary;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import java.io.File;
import java.lang.reflect.Method;

public class SerializedBeanFactory extends AbstractFactoryBean {

  private File _file;

  private Class<?> _objectType;

  private String _initMethod;

  public void setFile(File file) {
    _file = file;
  }

  public void setObjectType(Class<?> objectType) {
    _objectType = objectType;
  }

  public void setInitMethod(String initMethod) {
    _initMethod = initMethod;
  }

  @Override
  public Class<?> getObjectType() {
    return _objectType;
  }

  @Override
  protected Object createInstance() throws Exception {
    System.out.println("reading bean from file: " + _file);
    Object obj = ObjectSerializationLibrary.readObject(_file);
    if (_initMethod != null) {
      Class<? extends Object> c = obj.getClass();
      for (Method method : c.getMethods()) {
        if (method.getName().equals(_initMethod))
          if (method.getParameterTypes().length > 0) {
            System.err.println("cannot call init method " + _initMethod
                + " on " + obj + " because method requires arguments");
          } else {
            method.invoke(obj);
          }
      }
    }
    System.out.println("  complete");
    return obj;
  }
}
