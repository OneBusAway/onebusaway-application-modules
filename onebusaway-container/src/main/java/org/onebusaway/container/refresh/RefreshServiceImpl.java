package org.onebusaway.container.refresh;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ReflectionUtils;

class RefreshServiceImpl implements RefreshService, BeanPostProcessor {

  private static Logger _log = LoggerFactory.getLogger(RefreshServiceImpl.class);

  private Map<String, List<ObjectMethodPair>> _refreshMethodsByName = new HashMap<String, List<ObjectMethodPair>>();

  /****
   * {@link RefreshService} Interface
   ****/

  @Override
  public void refresh(String name) {
    List<ObjectMethodPair> pairs = _refreshMethodsByName.get(name);
    if (pairs != null) {
      for (ObjectMethodPair pair : pairs)
        invokePair(pair);
    }
  }

  /****
   * {@link BeanPostProcessor} Interface
   ****/

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName)
      throws BeansException {
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName)
      throws BeansException {

    visitClass(bean, bean.getClass());

    return bean;
  }

  /****
   * Private Methods
   ****/

  private void visitClass(Object target, Class<? extends Object> clazz) {

    Class<?> superclass = clazz.getSuperclass();
    if (superclass != null)
      visitClass(target, superclass);

    for (Class<?> interfaceClass : clazz.getInterfaces())
      visitClass(target, interfaceClass);

    for (Method method : clazz.getDeclaredMethods()) {
      Refreshable r = method.getAnnotation(Refreshable.class);

      if (r != null) {

        Class<?>[] params = method.getParameterTypes();

        if (params.length > 0) {
          _log.warn("@Refreshable methods cannot have arguments: " + method);
          continue;
        }

        for (String resourceName : r.dependsOn()) {
          List<ObjectMethodPair> pairs = _refreshMethodsByName.get(resourceName);
          if (pairs == null) {
            pairs = new ArrayList<ObjectMethodPair>();
            _refreshMethodsByName.put(resourceName, pairs);
          }

          pairs.add(new ObjectMethodPair(target, method));
        }
      }
    }
  }

  private void invokePair(ObjectMethodPair pair) {
    Object object = pair.getObject();
    Method method = pair.getMethod();
    try {
      ReflectionUtils.makeAccessible(method);
      method.invoke(object);
    } catch (Exception ex) {
      throw new IllegalStateException("error invoking refresh method=" + method
          + " on target object=" + object, ex);
    }
  }

  private static class ObjectMethodPair {
    private final Object object;
    private final Method method;

    public ObjectMethodPair(Object object, Method method) {
      this.object = object;
      this.method = method;
    }

    public Object getObject() {
      return object;
    }

    public Method getMethod() {
      return method;
    }
  }
}
