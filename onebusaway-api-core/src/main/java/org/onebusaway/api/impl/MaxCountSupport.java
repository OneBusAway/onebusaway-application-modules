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
package org.onebusaway.api.impl;

import java.util.Collections;
import java.util.List;

public class MaxCountSupport {

  private int _maxCount;

  private int _absoluteMaxCount;

  public MaxCountSupport() {
    _maxCount = Integer.MAX_VALUE;
    _absoluteMaxCount = Integer.MAX_VALUE;
  }

  public MaxCountSupport(int defaultMaxCount, int absoluteMaxCount) {
    _maxCount = defaultMaxCount;
    _absoluteMaxCount = absoluteMaxCount;
  }

  public void setMaxCount(int maxCount) {
    _maxCount = maxCount;
  }

  public int getMaxCount() {
    return Math.min(_maxCount, _absoluteMaxCount);
  }

  public <T> List<T> filter(List<T> values, boolean shuffle) {

    int maxCount = getMaxCount();

    if (values.size() > maxCount) {
      if (shuffle)
        Collections.shuffle(values);
      while (values.size() > maxCount)
        values.remove(values.size() - 1);
    }
    
    return values;
  }
}
