package org.onebusaway.webapp.gwt.common.model;

import java.util.ArrayList;
import java.util.List;

public class AbstractModel<T> implements ModelEventSourceAndSink<T>{

  private List<ModelListener<T>> _listeners = new ArrayList<ModelListener<T>>();

  public void addModelListener(ModelListener<T> listener) {
    _listeners.add(listener);
  }

  public void removeModelListener(ModelListener<T> listener) {
    _listeners.remove(listener);
  }

  public void fireModelChange(T model) {
    for (ModelListener<T> listener : _listeners)
      listener.handleUpdate(model);
  }

}
