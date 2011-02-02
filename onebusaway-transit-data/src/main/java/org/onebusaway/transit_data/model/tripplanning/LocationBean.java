package org.onebusaway.transit_data.model.tripplanning;

import java.io.Serializable;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.transit_data.model.StopBean;

public class LocationBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String name = null;

  private CoordinatePoint location = null;

  private StopBean stopBean;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public CoordinatePoint getLocation() {
    return location;
  }

  public void setLocation(CoordinatePoint location) {
    this.location = location;
  }

  public StopBean getStopBean() {
    return stopBean;
  }

  public void setStopBean(StopBean stopBean) {
    this.stopBean = stopBean;
  }
}
