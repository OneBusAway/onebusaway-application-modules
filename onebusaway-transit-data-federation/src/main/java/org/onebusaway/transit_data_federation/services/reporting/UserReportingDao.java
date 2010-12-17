package org.onebusaway.transit_data_federation.services.reporting;

import java.util.List;

import org.onebusaway.collections.tuple.T2;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.problems.EProblemReportStatus;
import org.onebusaway.transit_data_federation.impl.reporting.StopProblemReportRecord;
import org.onebusaway.transit_data_federation.impl.reporting.TripProblemReportRecord;

public interface UserReportingDao {

  public void saveOrUpdate(Object record);

  public void delete(Object entity);

  public List<T2<AgencyAndId, Integer>> getStopProblemReportSummaries(
      String agencyId, long timeFrom, long timeTo, EProblemReportStatus status);

  public List<T2<AgencyAndId, Integer>> getTripProblemReportSummaries(
      String agencyId, long timeFrom, long timeTo, EProblemReportStatus status);

  public List<StopProblemReportRecord> getStopProblemReports(String agencyId,
      long timeFrom, long timeTo, EProblemReportStatus status);
  
  public List<TripProblemReportRecord> getTripProblemReports(String agencyId,
      long timeFrom, long timeTo, EProblemReportStatus status);

  public List<StopProblemReportRecord> getAllStopProblemReportsForStopId(
      AgencyAndId stopId);

  public List<TripProblemReportRecord> getAllTripProblemReportsForTripId(
      AgencyAndId tripId);

  public StopProblemReportRecord getStopProblemRecordForId(long id);

  public TripProblemReportRecord getTripProblemRecordForId(long id);
}
