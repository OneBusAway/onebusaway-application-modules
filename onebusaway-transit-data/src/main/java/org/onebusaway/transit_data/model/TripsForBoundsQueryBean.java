package org.onebusaway.transit_data.model;

import java.io.Serializable;

import org.onebusaway.geospatial.model.CoordinateBounds;

@QueryBean
public final class TripsForBoundsQueryBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private CoordinateBounds bounds;

  private long time = System.currentTimeMillis();

  private int maxCount = 0;

  private boolean includeTripBeans = false;

  private boolean includeTripSchedules = false;

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

  public boolean isIncludeTripBeans() {
    return includeTripBeans;
  }

  public void setIncludeTripBeans(boolean includeTripBeans) {
    this.includeTripBeans = includeTripBeans;
  }

  public boolean isIncludeTripSchedules() {
    return includeTripSchedules;
  }

  public void setIncludeTripSchedules(boolean includeTripSchedules) {
    this.includeTripSchedules = includeTripSchedules;
  }

}
