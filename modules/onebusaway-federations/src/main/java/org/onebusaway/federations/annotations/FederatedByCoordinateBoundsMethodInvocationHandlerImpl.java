package org.onebusaway.federations.annotations;

import org.onebusaway.exceptions.ServiceAreaServiceException;
import org.onebusaway.federations.FederatedService;
import org.onebusaway.federations.FederatedServiceRegistry;
import org.onebusaway.geospatial.model.CoordinateBounds;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

class FederatedByCoordinateBoundsMethodInvocationHandlerImpl implements
    FederatedServiceMethodInvocationHandler {

  private int _argumentIndex;

  private Method[] _propertyReaders;
  

  public int getArgumentIndex() {
    return _argumentIndex;
  }
  
  public Method[] getPropertyReaders() {
    return _propertyReaders;
  }

  public FederatedByCoordinateBoundsMethodInvocationHandlerImpl(Method method,
      int argumentIndex, String expression) {
    _argumentIndex = argumentIndex;
    if (expression == null || expression.length() == 0) {
      _propertyReaders = new Method[0];
    } else {

      Class<?>[] parameterTypes = method.getParameterTypes();
      Class<?> currentType = parameterTypes[_argumentIndex];
      String[] propertyNames = expression.split("\\.");

      List<Method> propertyReaders = new ArrayList<Method>();

      for (String propertyName : propertyNames) {
        try {

          BeanInfo beanInfo = Introspector.getBeanInfo(currentType);
          PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
          PropertyDescriptor descriptor = getPropertyDescriptorForName(
              propertyDescriptors, propertyName);

          if (descriptor == null)
            throw new IllegalStateException("type " + currentType.getName()
                + " has no property named " + propertyName);

          Method readMethod = descriptor.getReadMethod();
          propertyReaders.add(readMethod);
          currentType = readMethod.getReturnType();

        } catch (IntrospectionException ex) {
          throw new IllegalStateException("error on introspection of type "
              + currentType.getName(), ex);
        }
      }
      
      _propertyReaders = propertyReaders.toArray(new Method[propertyReaders.size()]);
    }
  }

  public Object invoke(FederatedServiceRegistry registry, Method method,
      Object[] args) throws ServiceAreaServiceException,
      IllegalArgumentException, IllegalAccessException,
      InvocationTargetException {

    Object value = args[_argumentIndex];

    for (Method propertyReader : _propertyReaders)
      value = propertyReader.invoke(value);

    CoordinateBounds bounds = (CoordinateBounds) value;

    FederatedService service = registry.getServiceForBounds(bounds);
    return method.invoke(service, args);
  }

  private PropertyDescriptor getPropertyDescriptorForName(
      PropertyDescriptor[] propertyDescriptors, String name) {
    for (PropertyDescriptor descriptor : propertyDescriptors) {
      if (name.equals(descriptor.getName()))
        return descriptor;
    }
    return null;
  }

}
