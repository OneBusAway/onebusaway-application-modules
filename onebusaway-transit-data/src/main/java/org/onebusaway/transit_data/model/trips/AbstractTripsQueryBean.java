package org.onebusaway.transit_data.model.trips;

import java.io.Serializable;

import org.onebusaway.transit_data.model.QueryBean;

@QueryBean
public abstract class AbstractTripsQueryBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private long time = System.currentTimeMillis();

  private int maxCount = 0;

  private TripDetailsInclusionBean inclusion = new TripDetailsInclusionBean();

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
