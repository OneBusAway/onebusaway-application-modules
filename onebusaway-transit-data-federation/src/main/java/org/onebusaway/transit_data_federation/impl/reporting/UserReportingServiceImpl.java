package org.onebusaway.transit_data_federation.impl.reporting;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.ReportProblemWithTripBean;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.realtime.TripPosition;
import org.onebusaway.transit_data_federation.services.realtime.TripPositionService;
import org.onebusaway.transit_data_federation.services.reporting.UserReportingDao;
import org.onebusaway.transit_data_federation.services.reporting.UserReportingService;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripInstanceProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class UserReportingServiceImpl implements UserReportingService {

  private UserReportingDao _userReportingDao;

  private TransitGraphDao _graph;

  private TripPositionService _tripPositionService;

  @Autowired
  public void setUserReportingDao(UserReportingDao userReportingDao) {
    _userReportingDao = userReportingDao;
  }

  @Autowired
  public void setGrah(TransitGraphDao graph) {
    _graph = graph;
  }

  @Autowired
  public void setTripPositionService(TripPositionService tripPositionService) {
    _tripPositionService = tripPositionService;
  }

  @Override
  public void reportProblemWithTrip(ReportProblemWithTripBean problem) {

    AgencyAndId tripId = AgencyAndIdLibrary.convertFromString(problem.getTripId());

    TripEntry trip = _graph.getTripEntryForId(tripId);

    if (trip == null)
      return;

    TripProblemReportRecord record = new TripProblemReportRecord();
    record.setData(problem.getData());
    record.setServiceDate(problem.getServiceDate());

    String stopId = problem.getStopId();
    if (stopId != null)
      record.setStopId(AgencyAndIdLibrary.convertFromString(stopId));

    record.setTime(problem.getTime());
    record.setTripId(tripId);

    record.setUserComment(problem.getUserComment());
    record.setUserLat(problem.getUserLat());
    record.setUserLon(problem.getUserLon());
    record.setUserLocationAccuracy(problem.getUserLocationAccuracy());
    record.setUserOnVehicle(problem.isUserOnVehicle());
    record.setUserVehicleNumber(problem.getUserVehicleNumber());
    
    TripInstanceProxy tripInstance = new TripInstanceProxy(trip,
        problem.getServiceDate());

    TripPosition tripPosition = _tripPositionService.getPositionForTripInstance(
        tripInstance, problem.getTime());

    if (tripPosition != null) {
      record.setPredicted(tripPosition.isPredicted());
      record.setScheduleDeviation(tripPosition.getScheduleDeviation());
      CoordinatePoint p = tripPosition.getPosition();
      if (p != null) {
        record.setVehicleLat(p.getLat());
        record.setVehicleLon(p.getLon());
      }
      record.setVehicleId(tripPosition.getVehicleId());
    }

    _userReportingDao.saveOrUpdate(record);
  }
}
