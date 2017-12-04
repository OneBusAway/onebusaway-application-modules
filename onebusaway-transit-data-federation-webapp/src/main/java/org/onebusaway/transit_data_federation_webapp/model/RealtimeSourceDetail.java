/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data_federation_webapp.model;

import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.GtfsRealtimeSource;

public class RealtimeSourceDetail {

  private GtfsRealtimeSource source;
  private int index;
  private String baseUrl;
  
  public void setSource(GtfsRealtimeSource gtfsRealtimeSource) {
    source = gtfsRealtimeSource;
  }
  
  public GtfsRealtimeSource getSource() {
    return source;
  }
  
  public void setIndex(int i) {
    index = i;
  }
  
  public int getIndex() {
    return index;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }
}
