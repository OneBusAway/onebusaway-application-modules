package org.onebusaway.api.model.transit.service_alerts;

import java.io.Serializable;
import java.util.List;

import org.onebusaway.geospatial.model.EncodedPolylineBean;

public class SituationConditionDetailsV2Bean implements Serializable {

  private static final long serialVersionUID = 1L;

  private EncodedPolylineBean diversionPath;

  private List<String> diversionStopIds;

  public EncodedPolylineBean getDiversionPath() {
    return diversionPath;
  }

  public void setDiversionPath(EncodedPolylineBean diversionPath) {
    this.diversionPath = diversionPath;
  }

  public List<String> getDiversionStopIds() {
    return diversionStopIds;
  }

  public void setDiversionStopIds(List<String> diversionStopIds) {
    this.diversionStopIds = diversionStopIds;
  }
}
