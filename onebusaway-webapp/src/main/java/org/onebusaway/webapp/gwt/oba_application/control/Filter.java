package org.onebusaway.webapp.gwt.oba_application.control;

public interface Filter<T> {
  public boolean isEnabled(T element);
}
