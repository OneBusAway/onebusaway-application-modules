package org.onebusaway.transit_data_federation.impl.tripplanner.offline;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;

public class StopEntryImpl implements StopEntry, Serializable {

  private static final long serialVersionUID = 1L;

  private final AgencyAndId _id;

  private final double _lat;

  private final double _lon;

  private final StopTimeIndexImpl _index;

  private StopIdsWithValuesImpl _transfers = new StopIdsWithValuesImpl();

  private StopIdsWithValuesImpl _prevStopsWithMinTravelTime = new StopIdsWithValuesImpl();

  private StopIdsWithValuesImpl _nextStopsWithMinTravelTime = new StopIdsWithValuesImpl();

  public StopEntryImpl(AgencyAndId id, double lat, double lon,
      StopTimeIndexImpl index) {
    if (id == null || index == null)
      throw new IllegalArgumentException("id and index must not be null");
    _id = id;
    _lat = lat;
    _lon = lon;
    _index = index;
  }

  public AgencyAndId getId() {
    return _id;
  }

  public double getStopLat() {
    return _lat;
  }

  public double getStopLon() {
    return _lon;
  }

  public CoordinatePoint getStopLocation() {
    return new CoordinatePoint(_lat, _lon);
  }

  public StopTimeIndexImpl getStopTimes() {
    return _index;
  }

  public StopIdsWithValuesImpl getTransfers() {
    return _transfers;
  }

  public void addTransfer(StopEntry stop, double distance) {
    _transfers.setValue(stop, (int) distance);
  }

  public StopIdsWithValuesImpl getPreviousStopsWithMinTimes() {
    return _prevStopsWithMinTravelTime;
  }

  public void addPreviousStopWithMinTravelTime(StopEntry stop, int travelTime) {
    _prevStopsWithMinTravelTime.setMinValue(stop, travelTime);
  }

  public StopIdsWithValuesImpl getNextStopsWithMinTimes() {
    return _nextStopsWithMinTravelTime;
  }

  public void addNextStopWithMinTravelTime(StopEntry stop, int travelTime) {
    _nextStopsWithMinTravelTime.setMinValue(stop, travelTime);
  }

  /****
   * {@link Object} Interface
   ****/

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof StopEntryImpl))
      return false;
    StopEntryImpl stop = (StopEntryImpl) obj;
    return _id.equals(stop.getId());
  }

  @Override
  public int hashCode() {
    return _id.hashCode();
  }
  
  @Override
  public String toString() {
    return "StopEntry(id=" + _id+ ")";
  }

  /*****************************************************************************
   * Serialization Support
   ****************************************************************************/

  private void readObject(ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    in.defaultReadObject();
    TripPlannerGraphImpl.handleStopEntryRead(this);
  }

}
