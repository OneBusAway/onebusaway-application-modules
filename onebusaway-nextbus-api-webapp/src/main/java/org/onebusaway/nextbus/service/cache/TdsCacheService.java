package org.onebusaway.nextbus.service.cache;

import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.StopBean;

public interface TdsCacheService {

    AgencyBean getCachedAgencyBean(String id);
    StopBean getCachedStopBean(String id);
}
