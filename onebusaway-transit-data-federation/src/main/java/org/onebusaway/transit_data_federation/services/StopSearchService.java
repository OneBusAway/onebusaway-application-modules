package org.onebusaway.transit_data_federation.services;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.model.SearchResult;

import org.apache.lucene.queryParser.ParseException;

import java.io.IOException;

public interface StopSearchService {
  
  public SearchResult<AgencyAndId> searchForStopsByCode(String id, int maxResultCount, double minScoreToKeep) throws IOException, ParseException;

  public SearchResult<AgencyAndId> searchForStopsByName(String name, int maxResultCount, double minScoreToKeep) throws IOException,
      ParseException;
}
