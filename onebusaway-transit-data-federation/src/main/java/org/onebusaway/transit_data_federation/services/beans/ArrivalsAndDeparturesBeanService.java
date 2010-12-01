package org.onebusaway.transit_data_federation.services.beans;

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.ArrivalsAndDeparturesQueryBean;

/**
 * Service for querying arrivals and departures at a particular stop in a given
 * time range
 * 
 * @author bdferris
 * @see ArrivalAndDepartureBean
 */
public interface ArrivalsAndDeparturesBeanService {

  /**
   * @param stopId see {@link Stop#getId()}
   * @param timeFrom
   * @param timeTo
   * @return the list of arrival and departure beans for the specified stop in
   *         the specified time range
   */
  public List<ArrivalAndDepartureBean> getArrivalsAndDeparturesByStopId(
      AgencyAndId stopId, ArrivalsAndDeparturesQueryBean query);

  /**
   * Return arrival and departure information for a single trip instance
   * arriving and departing a particular stop.
   * 
   * @param stopId the target stop id
   * @param tripId the target tripId
   * @param serviceDate the target service date
   * @param vehicleId optionally specify a particular vehicle id, or null
   * @param time the time to query
   * @return the arrival and departure information, or null if not found
   */
  public ArrivalAndDepartureBean getArrivalAndDepartureForStop(
      AgencyAndId stopId, int stopSequence, AgencyAndId tripId, long serviceDate,
      AgencyAndId vehicleId, long time);
}
