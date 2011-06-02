package org.onebusaway.transit_data_federation.services;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.model.RouteCollection;
import org.onebusaway.transit_data_federation.model.SearchResult;

import org.apache.lucene.queryParser.ParseException;

import java.io.IOException;

/**
 * Service interface for searching for {@link RouteCollection} ids by route
 * short names and other parameters.
 * 
 * @author bdferris
 * @see RouteCollection
 */
public interface RouteCollectionSearchService {

  /**
   * 
   * @param nameQuery the route short name query
   * @param maxResultCount maximum number of results to keep
   * @param minScoreToKeep score tuning metric to prune result (implementation
   *          dependent)
   * @return a search result for {@link RouteCollection} ids matching the
   *         specified short name query
   * @throws IOException
   * @throws ParseException
   */
  public SearchResult<AgencyAndId> searchForRoutesByName(
      String nameQuery, int maxResultCount, double minScoreToKeep)
      throws IOException, ParseException;
}
