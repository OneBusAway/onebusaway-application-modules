package org.onebusaway.common.web.common.client.model;

public interface ModelEventSource<T> {
  
  public void addModelListener(ModelListener<T> listener);

  public void removeModelListener(ModelListener<T> listener);
}
