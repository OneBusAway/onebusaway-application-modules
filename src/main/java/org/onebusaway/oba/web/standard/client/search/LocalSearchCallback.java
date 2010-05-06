package org.onebusaway.oba.web.standard.client.search;

import org.onebusaway.oba.web.common.client.model.LocalSearchResult;

import java.util.List;

public interface LocalSearchCallback {
  public void onSuccess(List<LocalSearchResult> results);

  public void onFailure(Throwable ex);
}
