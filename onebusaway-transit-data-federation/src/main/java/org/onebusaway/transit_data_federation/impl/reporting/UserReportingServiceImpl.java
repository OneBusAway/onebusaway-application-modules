package org.onebusaway.transit_data_federation.impl.reporting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.StopProblemReportBean;
import org.onebusaway.transit_data.model.TripProblemReportBean;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.BlockStatusService;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;
import org.onebusaway.transit_data_federation.services.reporting.UserReportingDao;
import org.onebusaway.transit_data_federation.services.reporting.UserReportingService;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class UserReportingServiceImpl implements UserReportingService {

  private UserReportingDao _userReportingDao;

  private TransitGraphDao _graph;

  private BlockStatusService _blockStatusService;

  @Autowired
  public void setUserReportingDao(UserReportingDao userReportingDao) {
    _userReportingDao = userReportingDao;
  }

  @Autowired
  public void setGrah(TransitGraphDao graph) {
    _graph = graph;
  }

  @Autowired
  public void setBlockStatusService(BlockStatusService blockStatusService) {
    _blockStatusService = blockStatusService;
  }

  @Override
  public void reportProblemWithStop(StopProblemReportBean problem) {

    StopProblemReportRecord record = new StopProblemReportRecord();

    String stopId = problem.getStopId();
    if (stopId != null)
      record.setStopId(AgencyAndIdLibrary.convertFromString(stopId));

    record.setTime(problem.getTime());
    record.setData(problem.getData());
    record.setUserComment(problem.getUserComment());

    if (!Double.isNaN(problem.getUserLat()))
      record.setUserLat(problem.getUserLat());
    if (!Double.isNaN(problem.getUserLon()))
      record.setUserLon(problem.getUserLon());
    if (!Double.isNaN(problem.getUserLocationAccuracy()))
      record.setUserLocationAccuracy(problem.getUserLocationAccuracy());

    _userReportingDao.saveOrUpdate(record);
  }

  @Override
  public void reportProblemWithTrip(TripProblemReportBean problem) {

    AgencyAndId tripId = AgencyAndIdLibrary.convertFromString(problem.getTripId());

    TripEntry trip = _graph.getTripEntryForId(tripId);

    if (trip == null)
      return;

    BlockEntry block = trip.getBlock();

    TripProblemReportRecord record = new TripProblemReportRecord();
    record.setData(problem.getData());
    record.setServiceDate(problem.getServiceDate());

    String vehicleId = problem.getVehicleId();
    if (vehicleId != null)
      record.setVehicleId(AgencyAndIdLibrary.convertFromString(vehicleId));

    String stopId = problem.getStopId();
    if (stopId != null)
      record.setStopId(AgencyAndIdLibrary.convertFromString(stopId));

    record.setTime(problem.getTime());
    record.setTripId(tripId);
    record.setBlockId(block.getId());

    record.setUserComment(problem.getUserComment());

    if (!Double.isNaN(problem.getUserLat()))
      record.setUserLat(problem.getUserLat());
    if (!Double.isNaN(problem.getUserLon()))
      record.setUserLon(problem.getUserLon());
    if (!Double.isNaN(problem.getUserLocationAccuracy()))
      record.setUserLocationAccuracy(problem.getUserLocationAccuracy());

    record.setUserOnVehicle(problem.isUserOnVehicle());
    record.setUserVehicleNumber(problem.getUserVehicleNumber());

    Map<BlockInstance, List<BlockLocation>> locationsByInstance = _blockStatusService.getBlocks(
        block.getId(), problem.getServiceDate(), record.getVehicleId(),
        problem.getTime());

    BlockInstance blockInstance = getBestBlockInstance(locationsByInstance.keySet());

    if (blockInstance != null) {

      List<BlockLocation> blockLocations = locationsByInstance.get(blockInstance);

      BlockLocation blockLocation = getBestLocation(blockLocations, problem);

      if (blockLocation != null) {

        record.setPredicted(blockLocation.isPredicted());

        if (blockLocation.isDistanceAlongBlockSet())
          record.setDistanceAlongBlock(blockLocation.getDistanceAlongBlock());

        if (blockLocation.isScheduleDeviationSet())
          record.setScheduleDeviation(blockLocation.getScheduleDeviation());

        CoordinatePoint p = blockLocation.getLocation();
        if (p != null) {
          record.setVehicleLat(p.getLat());
          record.setVehicleLon(p.getLon());
        }
        record.setMatchedVehicleId(blockLocation.getVehicleId());
      }
    }

    _userReportingDao.saveOrUpdate(record);
  }

  @Override
  public List<TripProblemReportBean> getAllTripProblemReportsForTripId(
      AgencyAndId tripId) {
    List<TripProblemReportRecord> records = _userReportingDao.getAllTripProblemReportsForTripId(tripId);
    List<TripProblemReportBean> beans = new ArrayList<TripProblemReportBean>(
        records.size());
    for (TripProblemReportRecord record : records)
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
    if (record != null)
      _userReportingDao.delete(record);
  }

  /****
   * Private Methods
   ****/

  private BlockInstance getBestBlockInstance(
      Collection<BlockInstance> blockInstances) {

    if (blockInstances.isEmpty())
      return null;

    // Could we do something better here?
    return blockInstances.iterator().next();
  }

  private BlockLocation getBestLocation(List<BlockLocation> blockLocations,
      TripProblemReportBean problem) {

    if (blockLocations.isEmpty())
      return null;

    // Could we do something better here?
    return blockLocations.get(0);
  }

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
    bean.setDistanceAlongBlock(record.getDistanceAlongBlock());
    bean.setScheduleDeviation(record.getScheduleDeviation());
    bean.setVehicleLat(record.getVehicleLat());
    bean.setVehicleLon(record.getVehicleLon());
    return bean;
  }

}
