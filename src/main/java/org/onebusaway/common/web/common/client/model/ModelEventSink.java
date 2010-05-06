package org.onebusaway.common.web.common.client.model;

public interface ModelEventSink<T> {
  public void fireModelChange(T model);
}
