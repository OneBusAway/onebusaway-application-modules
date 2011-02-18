package org.onebusaway.api.model.transit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.transit_data.model.StopGroupingBean;

public class StopsForRouteV2Bean implements Serializable {

  private static final long serialVersionUID = 2L;

  private List<String> stopIds;

  private List<StopGroupingBean> stopGroupings;

  private List<EncodedPolylineBean> polylines;

  public List<String> getStopIds() {
    return stopIds;
  }

  public void setStopIds(List<String> stopIds) {
    this.stopIds = stopIds;
  }

  public List<StopGroupingBean> getStopGroupings() {
    return stopGroupings;
  }

  public void setStopGroupings(List<StopGroupingBean> groupings) {
    this.stopGroupings = groupings;
  }

  public List<EncodedPolylineBean> getPolylines() {
    return polylines;
  }

  public void setPolylines(List<EncodedPolylineBean> polylines) {
    this.polylines = polylines;
  }

  public void addGrouping(StopGroupingBean grouping) {
    if (stopGroupings == null)
      stopGroupings = new ArrayList<StopGroupingBean>();
    stopGroupings.add(grouping);
  }
}
