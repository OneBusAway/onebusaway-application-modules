package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.SearchQueryBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopsBean;

/**
 * Service methods for retrieving lists of stops, usually with a search query.
 * Note that a {@link StopBean} is our portable representation of a {@link Stop}
 * object.
 * 
 * @author bdferris
 * @see StopBean
 * @see Stop
 */
public interface StopsBeanService {

  /**
   * TODO: Convert this to use {@list ListBean} at some point?
   * 
   * @param query the stop search query
   * @return stops that match the specified query
   * @throws ServiceException
   */
  public StopsBean getStops(SearchQueryBean query) throws ServiceException;

  /**
   * 
   * @param agencyId see {@link Agency#getId()}
   * @return the list of all stops for the specified agency
   */
  public ListBean<String> getStopsIdsForAgencyId(String agencyId);
}
