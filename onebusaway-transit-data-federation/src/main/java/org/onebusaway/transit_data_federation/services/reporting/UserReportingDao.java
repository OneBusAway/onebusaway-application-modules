package org.onebusaway.transit_data_federation.services.reporting;

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.reporting.TripProblemReportRecord;

public interface UserReportingDao {

  public void saveOrUpdate(Object record);

  public List<TripProblemReportRecord> getAllTripProblemReportsForTripId(
      AgencyAndId tripId);

  public TripProblemReportRecord getTripProblemRecordForId(long id);

  public void delete(Object entity);

}
