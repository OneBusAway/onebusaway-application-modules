package org.onebusaway.transit_data_federation.impl.realtime.siri;

import java.util.ArrayList;
import java.util.List;

public class SiriEndpointDetails {

  private List<String> defaultAgencyIds = new ArrayList<String>();

  public List<String> getDefaultAgencyIds() {
    return defaultAgencyIds;
  }

  public void setDefaultAgencyIds(List<String> defaultAgencyIds) {
    this.defaultAgencyIds = defaultAgencyIds;
  }
}
