package org.onebusaway.oba.web.standard.client.control;

public interface Filter<T> {
  public boolean isEnabled(T element);
}
