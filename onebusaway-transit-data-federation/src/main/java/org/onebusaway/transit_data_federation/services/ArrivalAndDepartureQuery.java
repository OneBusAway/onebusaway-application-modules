package org.onebusaway.transit_data_federation.services;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;

public class ArrivalAndDepartureQuery {

  private StopEntry stop;

  private int stopSequence;

  private TripEntry trip;

  private long serviceDate;

  private AgencyAndId vehicleId;

  private long time;

  public StopEntry getStop() {
    return stop;
  }

  public void setStop(StopEntry stop) {
    this.stop = stop;
  }

  public int getStopSequence() {
    return stopSequence;
  }

  public void setStopSequence(int stopSequence) {
    this.stopSequence = stopSequence;
  }

  public TripEntry getTrip() {
    return trip;
  }

  public void setTrip(TripEntry trip) {
    this.trip = trip;
  }

  public long getServiceDate() {
    return serviceDate;
  }

  public void setServiceDate(long serviceDate) {
    this.serviceDate = serviceDate;
  }

  public AgencyAndId getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(AgencyAndId vehicleId) {
    this.vehicleId = vehicleId;
  }

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }
}
