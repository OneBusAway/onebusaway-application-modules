package org.onebusaway.webapp.gwt.oba_application.search;

import org.onebusaway.transit_data.model.oba.LocalSearchResult;

import java.util.List;

public interface LocalSearchCallback {
  public void onSuccess(List<LocalSearchResult> results);

  public void onFailure(Throwable ex);
}
