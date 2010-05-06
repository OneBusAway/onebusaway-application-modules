package org.onebusaway.tripplanner.web.common.client.model;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TripBean implements Serializable, IsSerializable {

  private static final long serialVersionUID = 1L;

  private List<TripSegmentBean> segments = new ArrayList<TripSegmentBean>();

  public List<TripSegmentBean> getSegments() {
    return segments;
  }

  public void setSegments(List<TripSegmentBean> segments) {
    this.segments = segments;
  }
}
