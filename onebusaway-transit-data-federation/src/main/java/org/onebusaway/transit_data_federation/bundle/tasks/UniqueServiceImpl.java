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
