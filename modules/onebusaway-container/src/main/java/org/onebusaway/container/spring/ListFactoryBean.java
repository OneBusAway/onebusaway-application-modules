package org.onebusaway.container.spring;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import java.util.ArrayList;
import java.util.List;

public class ListFactoryBean extends AbstractFactoryBean {

  private List<Object> _values = new ArrayList<Object>();

  public ListFactoryBean() {
    setSingleton(false);
  }

  public void setValues(List<Object> values) {
    _values = values;
  }

  public void addValues(List<Object> values) {
    _values.addAll(values);
  }

  @Override
  protected Object createInstance() throws Exception {
    List<Object> values = new ArrayList<Object>();
    if (_values != null)
      values.addAll(_values);
    return values;
  }

  @Override
  public Class<?> getObjectType() {
    return List.class;
  }
}
