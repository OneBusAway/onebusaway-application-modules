package org.onebusaway.transit_data.model;

import org.onebusaway.geospatial.model.EncodedPolylineBean;

import java.io.Serializable;
import java.util.List;

public class StopGroupBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private NameBean name;

  private List<String> stopIds;

  private List<StopGroupBean> subGroups;

  private List<EncodedPolylineBean> polylines;

  public NameBean getName() {
    return name;
  }

  public void setName(NameBean names) {
    this.name = names;
  }

  public List<String> getStopIds() {
    return stopIds;
  }

  public void setStopIds(List<String> stopIds) {
    this.stopIds = stopIds;
  }

  public List<StopGroupBean> getSubGroups() {
    return subGroups;
  }

  public void setSubGroups(List<StopGroupBean> subGroups) {
    this.subGroups = subGroups;
  }

  public List<EncodedPolylineBean> getPolylines() {
    return polylines;
  }

  public void setPolylines(List<EncodedPolylineBean> polylines) {
    this.polylines = polylines;
  }
}
