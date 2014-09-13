/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.watchdog.model;

import java.util.List;

import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.MonitoredDataSource;
import org.springframework.beans.factory.annotation.Autowired;

public class MetricConfiguration {

  private TransitDataService _tds;
  private List<MonitoredDataSource> _dataSources = null;
  
  @Autowired
  public void setTransitDataService(TransitDataService tds) {
    _tds = tds;
  }


  @Autowired
  public void setMonitoredDataSources(List<MonitoredDataSource> dataSources) {
    _dataSources = dataSources;
  }

  public List<MonitoredDataSource> getDataSources() {
    return _dataSources;
  }

  public TransitDataService getTDS() {
    return _tds;
  }
  
}
