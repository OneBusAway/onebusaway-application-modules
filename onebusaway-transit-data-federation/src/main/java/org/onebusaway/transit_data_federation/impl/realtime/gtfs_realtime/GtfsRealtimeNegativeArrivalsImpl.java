/**
 * Copyright (C) 2016 University of South Florida
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.PostConstruct;

@Component
public class GtfsRealtimeNegativeArrivalsImpl implements
    GtfsRealtimeNegativeArrivals, InitializingBean {
  
  private Map<String, Boolean> agencyNegativeScheduledArrivalMap = new HashMap<String, Boolean>();
  
  private Map<String,GtfsRealtimeSource> gtfsRealtimeSourceMap;
  
  @Autowired(required = false)
  public void setGtfsRealtimeSourceMap(
      Map<String, GtfsRealtimeSource> gtfsRealtimeSourceMap) {
    this.gtfsRealtimeSourceMap = gtfsRealtimeSourceMap;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    init();
  }

  @SuppressWarnings("rawtypes")
  @PostConstruct
  public void init() {
    if (gtfsRealtimeSourceMap == null) {
      return;
    }
    Iterator it = gtfsRealtimeSourceMap.entrySet().iterator();
    while (it.hasNext()) {
        Map.Entry pair = (Map.Entry)it.next();
        GtfsRealtimeSource grs = (GtfsRealtimeSource) pair.getValue();
        for (String id : grs.getAgencyIds()) {
          agencyNegativeScheduledArrivalMap.put(id, grs.getShowNegativeScheduledArrivals());
        }
    }
  }

  @Override
  public Boolean getShowNegativeScheduledArrivalByAgencyId(String agencyId) {
    return agencyNegativeScheduledArrivalMap.get(agencyId);
  }
}
