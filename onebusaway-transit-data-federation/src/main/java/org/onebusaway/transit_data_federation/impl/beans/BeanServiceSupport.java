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
package org.onebusaway.transit_data_federation.impl.beans;

import java.util.Collections;
import java.util.List;

public class BeanServiceSupport {

  public static <T> boolean checkLimitExceeded(List<T> elements, int maxCount) {
    boolean limitExceeded = elements.size() > maxCount;
    if (limitExceeded) {
      Collections.shuffle(elements);
      while (elements.size() > maxCount)
        elements.remove(elements.size() - 1);
    }
    return limitExceeded;
  }

}
