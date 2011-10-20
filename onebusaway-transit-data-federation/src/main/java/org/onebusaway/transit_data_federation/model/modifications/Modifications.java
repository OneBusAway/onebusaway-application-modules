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
package org.onebusaway.transit_data_federation.model.modifications;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Used for overriding values during narrative generation to spot-fix bad values
 * in the underlying data.
 * 
 * @author bdferris
 * 
 * @see Modification
 */
public class Modifications {

  private Map<String, Object> _modificationsByKey = new HashMap<String, Object>();

  @SuppressWarnings("unchecked")
  public <T> T getModificationForTypeAndId(Class<?> type, String id,
      String property) {
    String key = getKey(type, id, property);
    return (T) _modificationsByKey.get(key);
  }

  public void setModifications(List<Modification> modifications) {

    _modificationsByKey.clear();

    for (Modification modification : modifications) {
      Class<?> type = modification.getType();
      String id = modification.getId();
      String property = modification.getProperty();
      Object value = modification.getValue();
      String key = getKey(type, id, property);
      _modificationsByKey.put(key, value);
    }
  }

  private String getKey(Class<?> type, String id, String property) {
    return type.getName() + "[id=" + id + "]." + property;
  }
}
