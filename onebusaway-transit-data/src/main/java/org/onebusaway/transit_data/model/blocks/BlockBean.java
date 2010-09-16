package org.onebusaway.transit_data.model.blocks;

import java.io.Serializable;
import java.util.List;

import org.onebusaway.transit_data.model.trips.TripBean;

public class BlockBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String id;

  private List<TripBean> trips;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<TripBean> getTrips() {
    return trips;
  }

  public void setTrips(List<TripBean> trips) {
    this.trips = trips;
  }
}
