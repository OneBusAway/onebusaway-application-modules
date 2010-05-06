package org.onebusaway.transit_data_federation.model.modifications;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
