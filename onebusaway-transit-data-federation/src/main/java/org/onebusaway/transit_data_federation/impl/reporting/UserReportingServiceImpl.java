package org.onebusaway.transit_data_federation.impl.reporting;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.TripProblemReportBean;
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
  public void reportProblemWithTrip(TripProblemReportBean problem) {

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

  @Override
  public List<TripProblemReportBean> getAllTripProblemReportsForTripId(
      AgencyAndId tripId) {
    List<TripProblemReportRecord> records = _userReportingDao.getAllTripProblemReportsForTripId(tripId);
    List<TripProblemReportBean> beans = new ArrayList<TripProblemReportBean>(records.size());
    for(TripProblemReportRecord record : records)
      beans.add(getRecordAsBean(record));
    return beans;
  }

  @Override
  public TripProblemReportBean getTripProblemReportForId(long id) {
    TripProblemReportRecord record = _userReportingDao.getTripProblemRecordForId(id);
    return getRecordAsBean(record);
  }

  @Override
  public void deleteTripProblemReportForId(long id) {
    TripProblemReportRecord record = _userReportingDao.getTripProblemRecordForId(id);
    if( record != null)
      _userReportingDao.delete(record);
  }

  /****
   * Private Methods
   ****/

  private TripProblemReportBean getRecordAsBean(TripProblemReportRecord record) {
    TripProblemReportBean bean = new TripProblemReportBean();
    bean.setData(record.getData());
    bean.setId(record.getId());
    bean.setServiceDate(record.getServiceDate());
    bean.setStopId(AgencyAndIdLibrary.convertToString(record.getStopId()));
    bean.setTime(record.getTime());
    bean.setTripId(AgencyAndIdLibrary.convertToString(record.getTripId()));
    bean.setUserComment(record.getUserComment());
    bean.setUserLat(record.getUserLat());
    bean.setUserLon(record.getUserLon());
    bean.setUserLocationAccuracy(record.getUserLocationAccuracy());
    bean.setUserOnVehicle(record.isUserOnVehicle());
    bean.setUserVehicleNumber(record.getUserVehicleNumber());

    bean.setPredicted(record.isPredicted());
    bean.setPositionDeviation(record.getPositionDeviation());
    bean.setScheduleDeviation(record.getScheduleDeviation());
    bean.setVehicleLat(record.getVehicleLat());
    bean.setVehicleLon(record.getVehicleLon());
    return bean;
  }

}
