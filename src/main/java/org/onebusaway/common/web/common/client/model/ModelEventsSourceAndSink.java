package org.onebusaway.common.web.common.client.model;

public interface ModelEventsSourceAndSink extends ModelEventsSource, ModelEventsSink {
  public <T> ModelEventSourceAndSink<T> getEventSourceAndSink(Class<T> modelType);
}
