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
package org.onebusaway.transit_data_federation.bundle.tasks;

import java.util.HashMap;
import java.util.Map;

import org.onebusaway.transit_data_federation.bundle.services.UniqueService;
import org.springframework.stereotype.Component;

@Component
public class UniqueServiceImpl implements UniqueService {

  private Map<Object, Object> _values = new HashMap<Object, Object>();

  @SuppressWarnings("unchecked")
  @Override
  public <T> T unique(T object) {
    Object result = _values.get(object);
    if (result == null) {
      result = object;
      _values.put(object, result);
    }
    return (T) result;
  }
}
