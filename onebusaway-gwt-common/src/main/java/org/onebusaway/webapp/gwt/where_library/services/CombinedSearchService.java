package org.onebusaway.webapp.gwt.where_library.services;

import org.onebusaway.transit_data.model.SearchQueryBean;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface CombinedSearchService {
  public void search(SearchQueryBean query, int timeoutMillis,
      AsyncCallback<CombinedSearchResult> callback);
}
