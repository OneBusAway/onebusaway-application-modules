package org.onebusaway.transit_data_federation.services;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.transit_data_federation.model.SearchResult;

import org.apache.lucene.queryParser.ParseException;

import java.io.IOException;

/**
 * Service interface for searching for {@link Stop} ids by stop code and stop
 * name.
 * 
 * @author bdferris
 */
public interface StopSearchService {

  /**
   * Search for stop ids by stop code (see {@link Stop#getCode()}). Typically
   * default to a search against {@link Stop#getId()} if no code is specified
   * for a stop.
   * 
   * @param code the stop code query
   * @param maxResultCount maximum number of results to return
   * @param minScoreToKeep implementation-specific score cutoff for search
   *          results
   * @return a search result for matching stop ids
   * @throws IOException
   * @throws ParseException
   */
  public SearchResult<AgencyAndId> searchForStopsByCode(String code,
      int maxResultCount, double minScoreToKeep) throws IOException,
      ParseException;

  /**
   * Search for stop ids by stop name (see {@link Stop#getName()})
   * 
   * @param code the stop code query
   * @param maxResultCount maximum number of results to return
   * @param minScoreToKeep implementation-specific score cutoff for search
   *          results
   * @return a search result for matching stop ids
   * @throws IOException
   * @throws ParseException
   */
  public SearchResult<AgencyAndId> searchForStopsByName(String name,
      int maxResultCount, double minScoreToKeep) throws IOException,
      ParseException;
}
