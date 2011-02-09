package org.onebusaway.container.spring;

import org.onebusaway.utility.ObjectSerializationLibrary;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import java.io.File;
import java.lang.reflect.Method;

/**
 * A Spring {@link FactoryBean} for instantiating a serialized object from a
 * File.
 * 
 * @author bdferris
 */
public class SerializedFactoryBean extends AbstractFactoryBean {

  private File _path;

  private Class<?> _objectType;

  private String _initMethod;

  public void setPath(File path) {
    _path = path;
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
    System.out.println("reading bean from file: " + _path);
    Object obj = ObjectSerializationLibrary.readObject(_path);
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
