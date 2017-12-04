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

import java.io.Serializable;

import org.onebusaway.collections.beans.PropertyPathExpression;

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

  public CacheKeyInfo createKey(Object object) {
    object = _expression.invoke(object);
    return _objectKeyFactory.createKey(object);
  }
}
