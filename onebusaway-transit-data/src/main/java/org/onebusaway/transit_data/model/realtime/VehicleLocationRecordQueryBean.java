package org.onebusaway.transit_data.model.realtime;

import java.io.Serializable;

import org.onebusaway.transit_data.model.QueryBean;

@QueryBean
public class VehicleLocationRecordQueryBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String blockId;

  private String tripId;

  private long serviceDate;

  private String vehicleId;

  private long fromTime;

  private long toTime;

  public String getBlockId() {
    return blockId;
  }

  public void setBlockId(String blockId) {
    this.blockId = blockId;
  }

  public String getTripId() {
    return tripId;
  }

  public void setTripId(String tripId) {
    this.tripId = tripId;
  }

  public long getServiceDate() {
    return serviceDate;
  }

  public void setServiceDate(long serviceDate) {
    this.serviceDate = serviceDate;
  }

  public String getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(String vehicleId) {
    this.vehicleId = vehicleId;
  }

  public long getFromTime() {
    return fromTime;
  }

  public void setFromTime(long fromTime) {
    this.fromTime = fromTime;
  }

  public long getToTime() {
    return toTime;
  }

  public void setToTime(long toTime) {
    this.toTime = toTime;
  }
}
