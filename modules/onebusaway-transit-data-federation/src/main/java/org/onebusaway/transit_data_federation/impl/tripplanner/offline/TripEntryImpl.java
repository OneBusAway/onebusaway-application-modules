package org.onebusaway.transit_data_federation.impl.tripplanner.offline;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.List;

public class TripEntryImpl implements TripEntry, Serializable {

  private static final long serialVersionUID = 1L;

  private AgencyAndId _id;

  private AgencyAndId _routeId;

  private AgencyAndId _routeCollectionId;

  private AgencyAndId _serviceId;

  private List<StopTimeEntry> _stopTimes;

  private TripEntryImpl _prevTrip;

  private TripEntryImpl _nextTrip;

  public AgencyAndId getId() {
    return _id;
  }

  public void setId(AgencyAndId id) {
    _id = id;
  }

  public AgencyAndId getRouteId() {
    return _routeId;
  }

  public void setRouteId(AgencyAndId routeId) {
    _routeId = routeId;
  }

  public AgencyAndId getRouteCollectionId() {
    return _routeCollectionId;
  }

  public void setRouteCollectionId(AgencyAndId routeCollectionId) {
    _routeCollectionId = routeCollectionId;
  }

  public AgencyAndId getServiceId() {
    return _serviceId;
  }

  public void setServiceId(AgencyAndId serviceId) {
    _serviceId = serviceId;
  }

  public List<StopTimeEntry> getStopTimes() {
    return _stopTimes;
  }

  public void setStopTimes(List<StopTimeEntry> stopTimes) {
    _stopTimes = stopTimes;
  }

  public TripEntryImpl getPrevTrip() {
    return _prevTrip;
  }

  public void setPrevTrip(TripEntryImpl prevTrip) {
    _prevTrip = prevTrip;
  }

  public TripEntryImpl getNextTrip() {
    return _nextTrip;
  }

  public void setNextTrip(TripEntryImpl nextTrip) {
    _nextTrip = nextTrip;
  }

  private void readObject(ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    in.defaultReadObject();
    TripPlannerGraphImpl.handleTripEntryRead(this);
  }
  
  @Override
  public String toString() {
      return "Trip(" + _id + ")";
  }
}
