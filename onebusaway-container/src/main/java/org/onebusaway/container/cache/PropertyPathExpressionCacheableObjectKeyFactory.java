package org.onebusaway.container.cache;

import java.io.Serializable;

import org.onebusaway.collections.PropertyPathExpression;

/**
 * Factory for producing a {@link Serializable} cache key from an arbitrary
 * object by first applying a link {@link PropertyPathExpression} to the object
 * and then applying a secondary {@link CacheableObjectKeyFactory} to the
 * result.
 * 
 * @author bdferris
 * @see CacheableObjectKeyFactory
 * @see DefaultCacheableKeyFactory
 * @see PropertyPathExpression
 */
public class PropertyPathExpressionCacheableObjectKeyFactory implements
    CacheableObjectKeyFactory, Serializable {

  private static final long serialVersionUID = 1L;

  private final PropertyPathExpression _expression;

  private final CacheableObjectKeyFactory _objectKeyFactory;

  public PropertyPathExpressionCacheableObjectKeyFactory(
      PropertyPathExpression expression,
      CacheableObjectKeyFactory objectKeyFactory) {
    _expression = expression;
    _objectKeyFactory = objectKeyFactory;
  }

  public Serializable createKey(Object object) {
    object = _expression.invoke(object);
    return _objectKeyFactory.createKey(object);
  }
}
