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

import org.aspectj.lang.ProceedingJoinPoint;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Factory for producing a {@link CacheKeyInfo} cache key from a
 * {@link ProceedingJoinPoint} method invocation. The default implementation
 * considers each argument to the method invocation and applies a
 * {@link CacheableObjectKeyFactory} to produce a {@link Serializable} key value
 * for that argument. An array of those {@link Serializable} key values are
 * composed together to make the final cache key. The
 * {@link CacheKeyInfo#isCacheRefreshIndicated()} cache refresh indicators from
 * each argument key are ORed together to produce a refresh indicator for the
 * entire method.
 * 
 * @author bdferris
 * @see Cacheable#keyFactory()
 * @see CacheableMethodKeyFactory
 * @see CacheableObjectKeyFactory
 */
public class DefaultCacheableKeyFactory implements CacheableMethodKeyFactory,
    Serializable {

  private static final long serialVersionUID = 1L;

  private CacheableObjectKeyFactory[] _keyFactories;

  public DefaultCacheableKeyFactory(CacheableObjectKeyFactory[] keyFactories) {
    _keyFactories = keyFactories;
  }

  public int getNumberOfObjectKeyFactories() {
    return _keyFactories.length;
  }

  public CacheableObjectKeyFactory getObjectKeyFactory(int i) {
    return _keyFactories[i];
  }

  @Override
  public CacheKeyInfo createKey(ProceedingJoinPoint point) {
    Object[] args = point.getArgs();
    if (args.length != _keyFactories.length)
      throw new IllegalArgumentException();

    // Short circuit for single argument keys
    if (args.length == 1)
      return _keyFactories[0].createKey(args[0]);

    KeyImpl keys = new KeyImpl(args.length);
    boolean refreshCache = false;
    for (int i = 0; i < args.length; i++) {
      CacheKeyInfo keyInfo = _keyFactories[i].createKey(args[i]);
      keys.set(i, keyInfo.getKey());
      refreshCache |= keyInfo.isCacheRefreshIndicated();
    }
    return new CacheKeyInfo(keys, refreshCache);
  }

  static class KeyImpl implements Serializable {

    private static final long serialVersionUID = 1L;

    private Serializable[] _keys;

    public KeyImpl(int entries) {
      _keys = new Serializable[entries];
    }

    public KeyImpl(Serializable[] keys) {
      _keys = keys;
    }

    public void set(int index, Serializable key) {
      _keys[index] = key;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null || !(obj instanceof KeyImpl))
        return false;
      KeyImpl other = (KeyImpl) obj;
      return Arrays.equals(_keys, other._keys);
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(_keys);
    }

    @Override
    public String toString() {
      return Arrays.toString(_keys);
    }
  }

}
