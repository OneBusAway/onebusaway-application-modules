package org.onebusaway.transit_data.model.tripplanning;

import java.io.Serializable;

import org.onebusaway.geospatial.model.CoordinatePoint;

public class TransitLocationBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private double lat;

  private double lon;

  private String blockId;

  private long serviceDate;

  private String vehicleId;

  public TransitLocationBean() {

  }

  public TransitLocationBean(CoordinatePoint point) {
    this.lat = point.getLat();
    this.lon = point.getLon();
  }

  public CoordinatePoint getLocation() {
    return new CoordinatePoint(lat, lon);
  }

  public double getLat() {
    return lat;
  }

  public void setLat(double lat) {
    this.lat = lat;
  }

  public double getLon() {
    return lon;
  }

  public void setLon(double lon) {
    this.lon = lon;
  }

  public String getBlockId() {
    return blockId;
  }

  public void setBlockId(String blockId) {
    this.blockId = blockId;
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
}
