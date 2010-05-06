package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.transit_data.model.AgencyBean;

public interface AgencyBeanService {
  public AgencyBean getAgencyForId(String id);
}
