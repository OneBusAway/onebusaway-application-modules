package org.onebusaway.oba.web.standard.client.control;

public interface FilterListener<T> {
  public void onFilterChange(Filter<T> filter);
}
