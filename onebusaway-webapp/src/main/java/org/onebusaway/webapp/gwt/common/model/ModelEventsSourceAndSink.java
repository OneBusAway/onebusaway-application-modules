package org.onebusaway.webapp.gwt.common.model;

public interface ModelEventsSourceAndSink extends ModelEventsSource, ModelEventsSink {
  public <T> ModelEventSourceAndSink<T> getEventSourceAndSink(Class<T> modelType);
}
