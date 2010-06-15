package org.onebusaway.transit_data.model.trips;

import java.io.Serializable;

public final class TripDetailsInclusionBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private boolean includeTripBean = true;

  private boolean includeTripSchedule = true;

  private boolean includeTripStatus = true;

  public boolean isIncludeTripBean() {
    return includeTripBean;
  }

  public void setIncludeTripBean(boolean includeTripBean) {
    this.includeTripBean = includeTripBean;
  }

  public boolean isIncludeTripSchedule() {
    return includeTripSchedule;
  }

  public void setIncludeTripSchedule(boolean includeTripSchedule) {
    this.includeTripSchedule = includeTripSchedule;
  }

  public boolean isIncludeTripStatus() {
    return includeTripStatus;
  }

  public void setIncludeTripStatus(boolean includeTripStatus) {
    this.includeTripStatus = includeTripStatus;
  }
}
