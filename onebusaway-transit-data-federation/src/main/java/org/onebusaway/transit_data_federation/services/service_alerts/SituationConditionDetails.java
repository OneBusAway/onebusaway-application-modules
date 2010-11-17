package org.onebusaway.transit_data_federation.services.service_alerts;

import java.io.Serializable;
import java.util.List;

import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.gtfs.model.AgencyAndId;

public class SituationConditionDetails implements Serializable {

  private static final long serialVersionUID = 1L;

  private EncodedPolylineBean diversionPath;

  private List<AgencyAndId> diversionStops;

  public EncodedPolylineBean getDiversionPath() {
    return diversionPath;
  }

  public void setDiversionPath(EncodedPolylineBean diversionPath) {
    this.diversionPath = diversionPath;
  }

  public List<AgencyAndId> getDiversionStops() {
    return diversionStops;
  }

  public void setDiversionStops(List<AgencyAndId> diversionStops) {
    this.diversionStops = diversionStops;
  }
}
