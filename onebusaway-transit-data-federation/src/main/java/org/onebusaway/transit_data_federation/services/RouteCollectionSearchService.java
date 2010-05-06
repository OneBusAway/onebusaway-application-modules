package org.onebusaway.transit_data_federation.services;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.model.SearchResult;

import org.apache.lucene.queryParser.ParseException;

import java.io.IOException;

public interface RouteCollectionSearchService {

  public SearchResult<AgencyAndId> searchForRoutesByShortName(String value,
      int maxResultCount, double minScoreToKeep) throws IOException, ParseException;

}
