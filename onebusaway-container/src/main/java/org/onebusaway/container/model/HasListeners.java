package org.onebusaway.container.model;

public interface HasListeners<T> {

  public void addListener(T listener);

  public void removeListener(T listener);
}
