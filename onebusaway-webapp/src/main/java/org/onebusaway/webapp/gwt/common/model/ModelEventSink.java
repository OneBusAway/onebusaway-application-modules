package org.onebusaway.webapp.gwt.common.model;

public interface ModelEventSink<T> {
  public void fireModelChange(T model);
}
