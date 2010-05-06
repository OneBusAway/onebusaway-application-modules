package org.onebusaway.common.web.common.client.model;

public interface ModelListener<T> {
  public void handleUpdate(T model);
}
