package org.onebusaway.transit_data.model.trips;

import java.io.Serializable;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.transit_data.model.QueryBean;

@QueryBean
public final class TripsForBoundsQueryBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private CoordinateBounds bounds;

  private long time = System.currentTimeMillis();

  private int maxCount = 0;

  private TripDetailsInclusionBean inclusion = new TripDetailsInclusionBean();

  public CoordinateBounds getBounds() {
    return bounds;
  }

  public void setBounds(CoordinateBounds bounds) {
    this.bounds = bounds;
  }

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }

  public int getMaxCount() {
    return maxCount;
  }

  public void setMaxCount(int maxCount) {
    this.maxCount = maxCount;
  }

  public TripDetailsInclusionBean getInclusion() {
    return inclusion;
  }

  public void setInclusion(TripDetailsInclusionBean inclusion) {
    this.inclusion = inclusion;
  }
}
