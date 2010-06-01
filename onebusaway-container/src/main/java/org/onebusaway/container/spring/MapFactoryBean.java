package org.onebusaway.container.spring;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Spring {@link FactoryBean} for creating a {@link List} object.
 * 
 * @author bdferris
 * @see MapFactoryBean
 */
public class MapFactoryBean extends AbstractFactoryBean {

  private Map<Object, Object> _values = new HashMap<Object, Object>();

  @Override
  public Class<?> getObjectType() {
    return Map.class;
  }

  public void setValues(Map<Object, Object> values) {
    _values = values;
  }

  @Override
  protected Object createInstance() throws Exception {
    return new HashMap<Object, Object>(_values);
  }

  public void putValues(Map<Object, Object> values) {
    _values.putAll(values);
  }
}
