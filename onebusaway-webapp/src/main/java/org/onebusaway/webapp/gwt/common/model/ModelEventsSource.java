package org.onebusaway.webapp.gwt.common.model;

public interface ModelEventsSource {
  
  public <T> void addModelListener(Class<T> modelType, ModelListener<T> listener);

  public <T> void removeModelListener(Class<T> modelType, ModelListener<T> listener);
  
  public <T> ModelEventSource<T> getEventSource(Class<T> modelType);
}
