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
package org.onebusaway.webapp.actions.admin.vehiclepredictions;

import java.io.InputStream;
import java.util.List;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.onebusaway.gtfs_realtime.library.GtfsRealtimeConversionLibrary;
import org.onebusaway.gtfs_realtime.model.TripUpdateModel;
import org.onebusaway.gtfs_realtime.model.VehiclePositionModel;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.onebusaway.webapp.actions.OneBusAwayNYCAdminActionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.protobuf.ExtensionRegistry;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtimeOneBusAway;

@Namespace(value="/admin/vehiclepredictions")
@Result(name = "success",
  type = "json",
  params = {
      "excludeNullProperties", "true"
  })
public class GtfsRtProxyAction extends OneBusAwayNYCAdminActionSupport {

  private static final Logger _log = LoggerFactory.getLogger(GtfsRtProxyAction.class);
  
  private static final ExtensionRegistry _registry = ExtensionRegistry.newInstance();

  static {
    _registry.add(GtfsRealtimeOneBusAway.obaFeedEntity);
    _registry.add(GtfsRealtimeOneBusAway.obaTripUpdate);
  }
  
  @Autowired
  private ConfigurationService _configurationService;

  private List<TripUpdateModel> tripUpdates;
  
  private List<VehiclePositionModel> vehiclePositions;
  
  private String source;
  
  public String execute() {
    FeedMessage tu = getFeedMessage(getTripUpdatesPath());
    if (tu != null) {
      tripUpdates = GtfsRealtimeConversionLibrary.readTripUpdates(tu);
    }
   
    FeedMessage vp = getFeedMessage(getVehiclePositionsPath());
    if (vp != null) {
      vehiclePositions = GtfsRealtimeConversionLibrary.readVehiclePositions(vp);
    }
    
    return SUCCESS;
  }

  public List<TripUpdateModel> getTripUpdates() {
    return tripUpdates;
  }
  
  public List<VehiclePositionModel> getVehiclePositions() {
    return vehiclePositions;
  }
  
  public void setSource(String source) {
    this.source = source;
  }
  
  private FeedMessage getFeedMessage(String url) {
    try {
      CloseableHttpClient httpclient = HttpClients.createDefault();
      HttpGet request = new HttpGet(url);
      CloseableHttpResponse response = httpclient.execute(request);
      InputStream is = response.getEntity().getContent();
      FeedMessage message = FeedMessage.parseFrom(is, _registry);
      httpclient.close();
      return message;
    } catch (Exception e) {
      _log.error("Error retrieving trip updates: " + e);
    }
    
    return null;
  }
  
  private String getTripUpdatesPath() {
    return _configurationService.getConfigurationValueAsString("admin.gtfsrt." + source + ".trip_updates.url", "http://admin.staging.obast.org:9999/sc/trip-updates");
  }
  
  private String getVehiclePositionsPath() {
    return _configurationService.getConfigurationValueAsString("admin.gtfsrt." + source + ".vehicle_positions.url", "http://admin.staging.obast.org:9999/sc/vehicle-positions");
  }
 
}
