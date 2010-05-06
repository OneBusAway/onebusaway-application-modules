package org.onebusaway.gtfs.csv.schema;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class BeanWrapperFactory {

  private static Map<Class<?>, BeanClassWrapperImpl> _classWrappers = new HashMap<Class<?>, BeanClassWrapperImpl>();

  public static BeanWrapper wrap(Object object) {
    Class<? extends Object> c = object.getClass();
    BeanClassWrapperImpl classWrapper = _classWrappers.get(c);
    if (classWrapper == null) {
      try {
        BeanInfo beanInfo = java.beans.Introspector.getBeanInfo(c);
        classWrapper = new BeanClassWrapperImpl(beanInfo);
        _classWrappers.put(c, classWrapper);
      } catch (Exception ex) {
        throw new IllegalStateException("introspection error for type " + c, ex);
      }
    }

    return new BeanWrapperImpl(classWrapper, object);
  }

  private static class BeanClassWrapperImpl {

    private Map<String, Method> _readMethods = new HashMap<String, Method>();

    private Map<String, Method> _writeMethods = new HashMap<String, Method>();

    public BeanClassWrapperImpl(BeanInfo info) {
      PropertyDescriptor[] properties = info.getPropertyDescriptors();
      for (PropertyDescriptor property : properties) {
        String name = property.getName();
        _readMethods.put(name, property.getReadMethod());
        _writeMethods.put(name, property.getWriteMethod());
      }
    }

    public Object getPropertyValue(Object object, String propertyName) {
      Method method = _readMethods.get(propertyName);
      if (method == null)
        throw new IllegalArgumentException("no such property \"" + propertyName
            + "\" for type " + object.getClass());
      try {
        return method.invoke(object);
      } catch (Exception ex) {
        throw new IllegalStateException("error invoking getter for property \""
            + propertyName + "\" for type " + object.getClass(), ex);
      }
    }

    public void setPropertyValue(Object object, String propertyName,
        Object value) {
      Method method = _writeMethods.get(propertyName);
      if (method == null)
        throw new IllegalArgumentException("no such property \"" + propertyName
            + "\" for type " + object.getClass());
      try {
        method.invoke(object, value);
      } catch (Exception ex) {
        throw new IllegalStateException("error invoking setter for property \""
            + propertyName + "\" for type " + object.getClass(), ex);
      }
    }

  }

  private static class BeanWrapperImpl implements BeanWrapper {

    private BeanClassWrapperImpl _classWrapper;

    private Object _wrappedInstance;

    public BeanWrapperImpl(BeanClassWrapperImpl classWrapper,
        Object wrappedInstance) {
      _classWrapper = classWrapper;
      _wrappedInstance = wrappedInstance;
    }

    @SuppressWarnings("unchecked")
    public <T> T getWrappedInstance(Class<T> type) {
      return (T) _wrappedInstance;
    }

    public Object getPropertyValue(String propertyName) {
      return _classWrapper.getPropertyValue(_wrappedInstance, propertyName);
    }

    public void setPropertyValue(String propertyName, Object value) {
      _classWrapper.setPropertyValue(_wrappedInstance, propertyName, value);
    }

  }
}
