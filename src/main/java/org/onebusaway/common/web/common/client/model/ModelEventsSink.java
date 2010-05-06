package org.onebusaway.common.web.common.client.model;

public interface ModelEventsSink {

  public <T> void fireChange(T model);

  public <T, T2 extends T> void fireChange(Class<T> modelType, T2 model);

  public <T> ModelEventSink<T> getEventSink(Class<T> modelType);
}
