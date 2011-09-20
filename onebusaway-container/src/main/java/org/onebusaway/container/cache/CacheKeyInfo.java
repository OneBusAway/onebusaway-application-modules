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

/**
 * Encapsulates cache key information, including the cache key for a particular
 * method call and whether the cache should be refreshed for a particular method
 * call.
 * 
 * @author bdferris
 * @see CacheableMethodKeyFactory
 * @see CacheableObjectKeyFactory
 */
public final class CacheKeyInfo implements Serializable {

  private static final long serialVersionUID = 1L;

  private final Serializable key;

  private final boolean cacheRefreshIndicated;

  /**
   * 
   * @param key the serializable cache key generated for the method call
   * @param cacheRefreshIndicated true if the specified method call indicates
   *          that the cache should be cleared, otherwise false
   */
  public CacheKeyInfo(Serializable key, boolean cacheRefreshIndicated) {
    this.key = key;
    this.cacheRefreshIndicated = cacheRefreshIndicated;
  }

  /**
   * 
   * @return the serializable cache key generated for the method call
   */
  public Serializable getKey() {
    return key;
  }

  /**
   * 
   * @return true if the specified method call indicates that the cache should
   *         be cleared, otherwise false
   */
  public boolean isCacheRefreshIndicated() {
    return cacheRefreshIndicated;
  }

}
