package org.onebusaway.transit_data_federation.services.tripplanner;

public interface StopTimeEntry {

  public int getId();

  public TripEntry getTrip();

  public int getSequence();

  public StopEntry getStop();

  /**
   * @return arrival time, in seconds since midnight
   */
  public int getArrivalTime();

  /**
   * @return departure time, in seconds since midnight
   */
  public int getDepartureTime();

  public int getPickupType();

  public int getDropOffType();
}
