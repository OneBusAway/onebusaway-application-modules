package org.onebusaway.transit_data_federation.services.realtime;

import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;

public class ArrivalAndDepartureTime {

  private long arrivalTime;

  private long departureTime;

  public ArrivalAndDepartureTime(long arrivalTime, long departureTime) {
    this.arrivalTime = arrivalTime;
    this.departureTime = departureTime;
  }

  public long getArrivalTime() {
    return arrivalTime;
  }

  public void setArrivalTime(long arrivalTime) {
    this.arrivalTime = arrivalTime;
  }

  public long getDepartureTime() {
    return departureTime;
  }

  public void setDepartureTime(long departureTime) {
    this.departureTime = departureTime;
  }

  public static ArrivalAndDepartureTime getScheduledTime(
      BlockInstance blockInstance, BlockStopTimeEntry blockStopTime) {
    return getScheduledTime(blockInstance, blockStopTime, 0);
  }

  public static ArrivalAndDepartureTime getScheduledTime(
      BlockInstance blockInstance, BlockStopTimeEntry blockStopTime, int offset) {

    StopTimeEntry stopTime = blockStopTime.getStopTime();

    long arrivalTime = blockInstance.getServiceDate()
        + (stopTime.getArrivalTime() + offset) * 1000;
    long departureTime = blockInstance.getServiceDate()
        + (stopTime.getDepartureTime() + offset) * 1000;

    return new ArrivalAndDepartureTime(arrivalTime, departureTime);
  }
}
