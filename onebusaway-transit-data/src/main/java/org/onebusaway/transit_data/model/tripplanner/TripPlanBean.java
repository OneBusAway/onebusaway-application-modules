package org.onebusaway.transit_data.model.tripplanner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TripPlanBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private List<TripSegmentBean> segments = new ArrayList<TripSegmentBean>();

  public List<TripSegmentBean> getSegments() {
    return segments;
  }

  public void setSegments(List<TripSegmentBean> segments) {
    this.segments = segments;
  }
}
