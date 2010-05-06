package org.onebusaway.oba.web.standard.client.control;

public interface SourcesFilterEvents<T> {
  public void addFilterListener(FilterListener<T> listener);

  public void removeFilterListener(FilterListener<T> listener);
}
