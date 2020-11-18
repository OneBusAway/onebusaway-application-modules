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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.api.model.ResponseBean;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.VehicleStatusBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.model.trips.TripStatusBean;
import org.onebusaway.transit_data.services.TransitDataService;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition;

public class VehiclePositionsForAgencyActionTest {

  private VehiclePositionsForAgencyAction _action;

  private TransitDataService _service;

  @Before
  public void before() {
    _action = new VehiclePositionsForAgencyAction();

    _service = Mockito.mock(TransitDataService.class);
    _action.setTransitDataService(_service);
  }

  @Test
  public void test() {
    long now = System.currentTimeMillis();
    long v1Timestamp = now - 24*1000;
    long v2Timestamp = now - 11*1000;

    List<VehicleStatusBean> vehicles = new ArrayList<VehicleStatusBean>();
    RouteBean.Builder routeBuilder = RouteBean.builder();
    routeBuilder.setId("1_r1");
    RouteBean route = routeBuilder.create();

    {
      VehicleStatusBean vehicle = new VehicleStatusBean();
      vehicles.add(vehicle);
      vehicle.setLastUpdateTime(v1Timestamp);
      vehicle.setVehicleId("1_v1");

      TripStatusBean tripStatus = new TripStatusBean();
      vehicle.setTripStatus(tripStatus);

      TripBean trip = new TripBean();
      trip.setId("1_t0");
      trip.setRoute(route);
      tripStatus.setActiveTrip(trip);

      vehicle.setLocation(new CoordinatePoint(47.0, -122.0));
    }
    {
      VehicleStatusBean vehicle = new VehicleStatusBean();
      vehicles.add(vehicle);
      vehicle.setLastUpdateTime(v2Timestamp);
      vehicle.setVehicleId("1_v2");

      TripStatusBean tripStatus = new TripStatusBean();
      vehicle.setTripStatus(tripStatus);

      TripBean trip = new TripBean();
      trip.setId("1_t1");
      trip.setRoute(route);
      tripStatus.setActiveTrip(trip);

      vehicle.setLocation(new CoordinatePoint(47.1, -122.1));
    }

    ListBean<VehicleStatusBean> bean = new ListBean<VehicleStatusBean>();
    bean.setList(vehicles);
    Mockito.when(_service.getAllVehiclesForAgency("1", now)).thenReturn(bean);

    _action.setId("1");
    _action.setTime(new Date(now));

    _action.show();
    
    ResponseBean model = _action.getModel();
    FeedMessage feed = (FeedMessage) model.getData();
    assertEquals(now / 1000, feed.getHeader().getTimestamp());
    assertEquals(2, feed.getEntityCount());

    {
      FeedEntity entity = feed.getEntity(0);
      assertEquals("1", entity.getId());
      VehiclePosition vehiclePosition = entity.getVehicle();
      assertEquals("t0", vehiclePosition.getTrip().getTripId());
      assertEquals("r1", vehiclePosition.getTrip().getRouteId());
      assertEquals("v1", vehiclePosition.getVehicle().getId());
      assertEquals(v1Timestamp/1000, vehiclePosition.getTimestamp());
      assertEquals(47.0, vehiclePosition.getPosition().getLatitude(), 0.01);
      assertEquals(-122.0, vehiclePosition.getPosition().getLongitude(), 0.01);
    }

    {
      FeedEntity entity = feed.getEntity(1);
      assertEquals("2", entity.getId());
      VehiclePosition vehiclePosition = entity.getVehicle();
      assertEquals("t1", vehiclePosition.getTrip().getTripId());
      assertEquals("r1", vehiclePosition.getTrip().getRouteId());
      assertEquals("v2", vehiclePosition.getVehicle().getId());
      assertEquals(v2Timestamp/1000, vehiclePosition.getTimestamp());
      assertEquals(47.1, vehiclePosition.getPosition().getLatitude(), 0.01);
      assertEquals(-122.1, vehiclePosition.getPosition().getLongitude(), 0.01);
    }
  }
}
