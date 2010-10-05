package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;

import java.util.Date;
import java.util.List;

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
      AgencyAndId stopId, Date time, int minutesBefore, int minutesAfter);
}
