/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.container.cache;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.ehcache.Cache;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.onebusaway.collections.beans.PropertyPathExpression;
import org.springframework.stereotype.Component;

/**
 * Support class that determines the caching policy for a particular method by
 * producing a {@link CacheableMethodKeyFactory} for that method. We make use of
 * {@link Cacheable}, {@link CacheableArgument}, and {@link CacheableKey}
 * annotations to allow customization of the default key factory behavior, as
 * well as directly setting behavior using methods such as
 * {@link #setCacheKeyFactories(Map)} and
 * {@link #putCacheRefreshIndicatorArgumentIndexForMethod(Method, int)}.
 * 
 * @author bdferris
 * @see CacheableMethodKeyFactory
 * @see Cacheable
 * @see CacheableArgument
 * @see CacheableKey
 */
@Component
public class CacheableMethodKeyFactoryManager {

  private Map<Class<?>, CacheableObjectKeyFactory> _keyFactories = new HashMap<Class<?>, CacheableObjectKeyFactory>();

  private Map<Method, Integer> _cacheRefreshIndicatorArgumentIndexByMethod = new HashMap<Method, Integer>();

  public void addCacheableObjectKeyFactory(Class<?> className,
      CacheableObjectKeyFactory keyFactory) {
    _keyFactories.put(className, keyFactory);
  }

  public void setCacheKeyFactories(Map<Object, Object> keyFactories) {
    for (Map.Entry<Object, Object> entry : keyFactories.entrySet()) {
      Class<?> className = getObjectAsClass(entry.getKey());
      CacheableObjectKeyFactory keyFactory = getObjectAsObjectKeyFactory(entry.getValue());
      addCacheableObjectKeyFactory(className, keyFactory);
    }
  }

  public void putCacheRefreshIndicatorArgumentIndexForMethodSignature(
      String methodName, int argumentIndex) {
    Method method = getMethodForSignature(methodName);
    _cacheRefreshIndicatorArgumentIndexByMethod.put(method, argumentIndex);
  }

  public void putCacheRefreshIndicatorArgumentIndexForMethod(Method method,
      int argumentIndex) {
    _cacheRefreshIndicatorArgumentIndexByMethod.put(method, argumentIndex);
  }

  public CacheableMethodKeyFactory getCacheableMethodKeyFactoryForJoinPoint(
      ProceedingJoinPoint pjp, Method method) {
    return getCacheableMethodKeyFactoryForMethod(method);
  }

  public CacheableMethodKeyFactory getCacheableMethodKeyFactoryForMethod(
      Method m) {

    /**
     * Try looking for a @Cacheable annotation first and using that first for
     * the CacheableMethodKeyFactory if specified
     */
    Cacheable cacheableAnnotation = m.getAnnotation(Cacheable.class);

    if (cacheableAnnotation != null) {

      Class<? extends CacheableMethodKeyFactory> keyFactoryType = cacheableAnnotation.keyFactory();

      if (!keyFactoryType.equals(CacheableMethodKeyFactory.class))
        try {
          return keyFactoryType.newInstance();
        } catch (Exception ex) {
          throw new IllegalStateException(
              "error instantiating CacheableKeyFactory: "
                  + keyFactoryType.getName(), ex);
        }
    }

    /**
     * Revert to default cacheable method key factory behavior
     */
    Class<?>[] parameters = m.getParameterTypes();
    Annotation[][] annotations = m.getParameterAnnotations();

    int cacheRefreshParameterIndex = -1;
    if (_cacheRefreshIndicatorArgumentIndexByMethod.containsKey(m))
      cacheRefreshParameterIndex = _cacheRefreshIndicatorArgumentIndexByMethod.get(m);

    CacheableObjectKeyFactory[] keyFactories = new CacheableObjectKeyFactory[parameters.length];
    for (int i = 0; i < parameters.length; i++) {

      boolean cacheRefreshIndicator = (i == cacheRefreshParameterIndex);
      CacheableArgument cacheableArgumentAnnotation = getCacheableArgumentAnnotation(annotations[i]);

      if (cacheableArgumentAnnotation != null) {
        keyFactories[i] = getKeyFactoryForCacheableArgumentAnnotation(
            parameters[i], cacheableArgumentAnnotation, cacheRefreshIndicator);
      } else {
        keyFactories[i] = getKeyFactoryForParameterType(parameters[i],
            cacheRefreshIndicator);
      }
    }

    return new DefaultCacheableKeyFactory(keyFactories);
  }

  public Method getMatchingMethodForJoinPoint(ProceedingJoinPoint pjp) {
    List<Method> methods = getMatchingMethodsForJoinPoint(pjp);
    if (methods.size() == 1) {
      return methods.get(0);
    } else if (methods.size() == 0) {
      throw new IllegalArgumentException("method not found: pjp="
          + pjp.getSignature());
    } else {
      throw new IllegalArgumentException("multiple methods found: pjp="
          + pjp.getSignature());
    }
  }

  public List<Method> getMatchingMethodsForJoinPoint(ProceedingJoinPoint pjp) {

    Signature sig = pjp.getSignature();

    Object target = pjp.getTarget();
    Class<?> type = target.getClass();

    List<Method> matches = new ArrayList<Method>();

    for (Method m : type.getDeclaredMethods()) {
      if (!m.getName().equals(sig.getName()))
        continue;

      // if (m.getModifiers() != sig.getModifiers())
      // continue;
      Object[] args = pjp.getArgs();
      Class<?>[] types = m.getParameterTypes();
      if (args.length != types.length)
        continue;
      boolean miss = false;
      for (int i = 0; i < args.length; i++) {
        Object arg = args[i];
        Class<?> argType = types[i];
        if (argType.isPrimitive()) {
          if (argType.equals(Double.TYPE)
              && !arg.getClass().equals(Double.class))
            miss = true;
        } else {
          if (arg != null && !argType.isInstance(arg))
            miss = true;
        }
      }
      if (miss)
        continue;
      matches.add(m);
    }

    return matches;
  }

  /***************************************************************************
   * Private Methods
   **************************************************************************/

  protected CacheableArgument getCacheableArgumentAnnotation(
      Annotation[] annotations) {
    for (Annotation annotation : annotations) {
      if (annotation instanceof CacheableArgument)
        return (CacheableArgument) annotation;
    }
    return null;
  }

  protected CacheableObjectKeyFactory getKeyFactoryForCacheableArgumentAnnotation(
      Class<?> type, CacheableArgument cacheableArgumentAnnotation,
      boolean cacheRefreshIndicator) {

    String keyProperty = cacheableArgumentAnnotation.keyProperty();

    cacheRefreshIndicator |= cacheableArgumentAnnotation.cacheRefreshIndicator();

    if (!(keyProperty == null || keyProperty.equals(""))) {
      PropertyPathExpression expression = new PropertyPathExpression(
          keyProperty);
      type = expression.initialize(type);
      CacheableObjectKeyFactory factory = getKeyFactoryForParameterType(type,
          cacheRefreshIndicator);
      return new PropertyPathExpressionCacheableObjectKeyFactory(expression,
          factory);
    }

    // Nothing interesting defined in the annotation? Apply the default behavior
    return getKeyFactoryForParameterType(type, cacheRefreshIndicator);
  }

  protected CacheableObjectKeyFactory getKeyFactoryForParameterType(
      Class<?> type, boolean cacheRefreshIndicator) {

    if (_keyFactories.containsKey(type))
      return _keyFactories.get(type);

    for (Map.Entry<Class<?>, CacheableObjectKeyFactory> entry : _keyFactories.entrySet()) {
      Class<?> argumentType = entry.getKey();
      if (argumentType.isAssignableFrom(type))
        return entry.getValue();
    }

    Class<?> checkType = type;

    while (checkType != null && !checkType.equals(Object.class)) {
      CacheableKey annotation = checkType.getAnnotation(CacheableKey.class);

      if (annotation != null) {
        Class<? extends CacheableObjectKeyFactory> keyFactoryType = annotation.keyFactory();
        try {
          return keyFactoryType.newInstance();
        } catch (Exception ex) {
          throw new IllegalStateException(
              "error instantiating CacheableObjectKeyFactory [type="
                  + keyFactoryType.getName() + "] from CacheableKey [type="
                  + checkType.getName() + "]", ex);
        }
      }
      checkType = type.getSuperclass();
    }

    DefaultCacheableObjectKeyFactory factory = new DefaultCacheableObjectKeyFactory();
    factory.setCacheRefreshCheck(cacheRefreshIndicator);
    return factory;
  }

  protected Cache createCache(ProceedingJoinPoint pjp, String name) {
    return null;
  }

  /****
   * Private Methods
   ****/

  private Class<?> getObjectAsClass(Object object) {
    if (object instanceof Class<?>)
      return (Class<?>) object;
    if (object instanceof String) {
      try {
        return Class.forName((String) object);
      } catch (ClassNotFoundException e) {
        throw new IllegalArgumentException(e);
      }
    }
    throw new IllegalArgumentException("unable to convert object to class: "
        + object);
  }

  private CacheableObjectKeyFactory getObjectAsObjectKeyFactory(Object value) {
    if (value instanceof CacheableObjectKeyFactory)
      return (CacheableObjectKeyFactory) value;
    Class<?> classType = getObjectAsClass(value);
    if (!CacheableObjectKeyFactory.class.isAssignableFrom(classType))
      throw new IllegalArgumentException(classType + " is not assignable to "
          + CacheableObjectKeyFactory.class);
    try {
      return (CacheableObjectKeyFactory) classType.newInstance();
    } catch (Exception ex) {
      throw new IllegalStateException("error instantiating " + classType, ex);
    }
  }

  private Method getMethodForSignature(String methodSignature) {

    int index = methodSignature.lastIndexOf('.');
    if (index == -1)
      throw new IllegalArgumentException(
          "invalid method signature: expected=package.ClassName.methodName actual="
              + methodSignature);

    String className = methodSignature.substring(0, index);
    String methodName = methodSignature.substring(index + 1);
    Class<?> clazz = null;

    try {
      clazz = Class.forName(className);
    } catch (ClassNotFoundException ex) {
      throw new IllegalStateException(ex);
    }

    List<Method> methods = new ArrayList<Method>();
    for (Method method : clazz.getMethods()) {
      if (method.getName().equals(methodName))
        methods.add(method);
    }

    if (methods.size() == 1)
      return methods.get(0);
    else if (methods.size() == 0)
      throw new IllegalArgumentException("no method found for signature: "
          + methodSignature);
    else
      throw new IllegalArgumentException(
          "multiple methods found for signature: " + methodSignature);
  }

}