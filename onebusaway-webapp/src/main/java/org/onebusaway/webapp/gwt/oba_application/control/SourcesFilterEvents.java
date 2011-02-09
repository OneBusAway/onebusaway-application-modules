package org.onebusaway.webapp.gwt.oba_application.control;

public interface SourcesFilterEvents<T> {
  public void addFilterListener(FilterListener<T> listener);

  public void removeFilterListener(FilterListener<T> listener);
}
