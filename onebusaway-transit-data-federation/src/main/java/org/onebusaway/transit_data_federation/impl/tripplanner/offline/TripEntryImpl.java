package org.onebusaway.transit_data_federation.impl.tripplanner.offline;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;

public class TripEntryImpl implements TripEntry, Serializable {

  private static final long serialVersionUID = 4L;

  private AgencyAndId _id;

  private AgencyAndId _routeId;

  private AgencyAndId _routeCollectionId;

  private BlockEntryImpl _block;

  private AgencyAndId _serviceId;

  private AgencyAndId _shapeId;

  private int _stopTimeFromIndex;

  private int _stopTimeToIndex;

  private double _distanceAlongBlock;

  private TripEntryImpl _prevTrip;

  private TripEntryImpl _nextTrip;

  public void setId(AgencyAndId id) {
    _id = id;
  }

  public void setRouteId(AgencyAndId routeId) {
    _routeId = routeId;
  }

  public void setRouteCollectionId(AgencyAndId routeCollectionId) {
    _routeCollectionId = routeCollectionId;
  }

  public void setBlock(BlockEntryImpl block) {
    _block = block;
  }

  public void setServiceId(AgencyAndId serviceId) {
    _serviceId = serviceId;
  }
  
  public void setShapeId(AgencyAndId shapeId) {
    _shapeId = shapeId;
  }

  public void setStopTimeIndices(int fromIndex, int toIndex) {
    _stopTimeFromIndex = fromIndex;
    _stopTimeToIndex = toIndex;
  }

  public void setDistanceAlongBlock(double distanceAlongBlock) {
    _distanceAlongBlock = distanceAlongBlock;
  }

  public void setPrevTrip(TripEntryImpl prevTrip) {
    _prevTrip = prevTrip;
  }

  public void setNextTrip(TripEntryImpl nextTrip) {
    _nextTrip = nextTrip;
  }

  /****
   * {@link TripEntry} Interface
   ****/

  @Override
  public AgencyAndId getId() {
    return _id;
  }

  @Override
  public AgencyAndId getRouteId() {
    return _routeId;
  }

  @Override
  public AgencyAndId getRouteCollectionId() {
    return _routeCollectionId;
  }

  @Override
  public BlockEntryImpl getBlock() {
    return _block;
  }

  @Override
  public AgencyAndId getServiceId() {
    return _serviceId;
  }

  @Override
  public AgencyAndId getShapeId() {
    return _shapeId;
  }

  @Override
  public List<StopTimeEntry> getStopTimes() {
    return _block.getStopTimes().subList(_stopTimeFromIndex, _stopTimeToIndex);
  }

  @Override
  public int getBlockStopTimeSequenceOffset() {
    return _stopTimeFromIndex;
  }

  @Override
  public double getDistanceAlongBlock() {
    return _distanceAlongBlock;
  }

  @Override
  public TripEntryImpl getPrevTrip() {
    return _prevTrip;
  }

  @Override
  public TripEntryImpl getNextTrip() {
    return _nextTrip;
  }

  @Override
  public String toString() {
    return "Trip(" + _id + ")";
  }

  /****
   * Serialization
   ****/

  private void readObject(ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    in.defaultReadObject();
    TripPlannerGraphImpl.handleTripEntryRead(this);
  }
}
