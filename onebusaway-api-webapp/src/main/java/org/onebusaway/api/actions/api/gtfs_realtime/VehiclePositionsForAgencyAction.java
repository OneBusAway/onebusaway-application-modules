/**
 * Copyright (C) 2013 Google, Inc.
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
package org.onebusaway.api.actions.api.gtfs_realtime;

import com.google.transit.realtime.GtfsRealtime;

import org.apache.struts2.ServletActionContext;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.VehicleStatusBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.model.trips.TripStatusBean;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.Position;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.VehicleDescriptor;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition;
import org.onebusaway.util.AgencyAndIdLibrary;

import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class VehiclePositionsForAgencyAction extends GtfsRealtimeActionSupport {

  private static final long serialVersionUID = 1L;

  private static final SimpleDateFormat _sdf;

  static {
    _sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT' ", Locale.US);
    _sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
  }
  @Override
  protected void fillFeedMessage(FeedMessage.Builder feed, String agencyId,
      long timestamp, FILTER_TYPE filterType, String filterValue) {

    long feedTimestamp = feed.getHeader().getTimestamp();

    ListBean<VehicleStatusBean> vehicles = _service.getAllVehiclesForAgency(
        agencyId, timestamp);
    
    //Filter for last vehicle positions < 60'
    for (int i = vehicles.getList().size() - 1; i >= 0; i--) {
      if ((((timestamp / 1000) - (vehicles.getList().get(i).getLastUpdateTime() / 1000)) >= 600) || 
        vehicles.getList().get(i).getLocation() == null)
        vehicles.getList().remove(i);
    }

    for (VehicleStatusBean vehicle : vehicles.getList()) {
      boolean foundMatch = FILTER_TYPE.ROUTE_ID != filterType;

      FeedEntity.Builder entity = GtfsRealtime.FeedEntity.newBuilder();
      entity.setId(Integer.toString(feed.getEntityCount()+1));
      VehiclePosition.Builder vehiclePosition = entity.getVehicleBuilder();

      TripStatusBean tripStatus = vehicle.getTripStatus();
      if (tripStatus != null) {
        TripBean activeTrip = tripStatus.getActiveTrip();
        RouteBean route = activeTrip.getRoute();

        if (FILTER_TYPE.ROUTE_ID == filterType && !filterValue.equals(AgencyAndIdLibrary.convertFromString(route.getId()).getId())) {
          // skip this route
          continue;
        }

        TripDescriptor.Builder tripDesc = vehiclePosition.getTripBuilder();
        tripDesc.setTripId(normalizeId(activeTrip.getId()));
        tripDesc.setRouteId(normalizeId(route.getId()));
        if (FILTER_TYPE.ROUTE_ID == filterType && filterValue.equals(AgencyAndIdLibrary.convertFromString(route.getId()).getId())) {
          foundMatch = true;
        }
      }

      VehicleDescriptor.Builder vehicleDesc = vehiclePosition.getVehicleBuilder();
      vehicleDesc.setId(normalizeId(vehicle.getVehicleId()));

      CoordinatePoint location = vehicle.getLocation();
      if (location != null) {
        Position.Builder position = vehiclePosition.getPositionBuilder();
        position.setLatitude((float) location.getLat());
        position.setLongitude((float) location.getLon());
      }

      vehiclePosition.setTimestamp(vehicle.getLastUpdateTime() / 1000);
      if (foundMatch) {
        feed.addEntity(entity);
      }
    }
    if(feedTimestamp != 0){
      long lastModifiedMills = feedTimestamp * 1000L;
      String lastModifiedHeader = _sdf.format(new Date(lastModifiedMills));
      HttpServletResponse response = ServletActionContext.getResponse();
      response.setHeader("Last-Modified", lastModifiedHeader);
    } else {
      long lastModifiedMills = timestamp * 1000L;
      String lastModifiedHeader = _sdf.format(new Date(lastModifiedMills));

      HttpServletResponse response = ServletActionContext.getResponse();
      response.setHeader("Last-Modified", lastModifiedHeader);
    }
  }
}
