package org.onebusaway.webapp.gwt.common.model;

public interface ModelEventSource<T> {
  
  public void addModelListener(ModelListener<T> listener);

  public void removeModelListener(ModelListener<T> listener);
}
