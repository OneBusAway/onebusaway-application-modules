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
import static org.mockito.ArgumentMatchers.any;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.opensymphony.xwork2.ActionContext;
import org.apache.struts2.ServletActionContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.onebusaway.api.model.ResponseBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.VehicleStatusBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripStatusBean;
import org.onebusaway.transit_data.services.TransitDataService;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class TripUpdatesForAgencyActionTest {

  private TripUpdatesForAgencyAction _action;

  private TransitDataService _service;

  @Mock
  HttpServletRequest request;

  @Mock
  HttpServletResponse servletResponse;

  @Before
  public void before() {
    ActionContext.setContext(new ActionContext(new HashMap<>()));

    ServletActionContext.setRequest(request);
    ServletActionContext.setResponse(servletResponse);

    _action = new TripUpdatesForAgencyAction();

    _service = Mockito.mock(TransitDataService.class);
    _action.setTransitDataService(_service);
  }

  @Test
  public void test() {
    long now = System.currentTimeMillis();
    long v1Timestamp = now - 6*1000;
    long v2Timestamp = now - 4*1000;

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

      tripStatus.setScheduleDeviation(2 * 60);

      TripBean trip = new TripBean();
      trip.setId("1_t0");
      trip.setRoute(route);
      tripStatus.setActiveTrip(trip);

      StopBean stop = new StopBean();
      stop.setId("1_s2");
      tripStatus.setNextStop(stop);
      tripStatus.setNextStopTimeOffset(5 * 60);
    }
    {
      VehicleStatusBean vehicle = new VehicleStatusBean();
      vehicles.add(vehicle);
      vehicle.setLastUpdateTime(v2Timestamp);
      vehicle.setVehicleId("1_v2");

      TripStatusBean tripStatus = new TripStatusBean();
      vehicle.setTripStatus(tripStatus);

      tripStatus.setScheduleDeviation(3 * 60);

      TripBean trip = new TripBean();
      trip.setId("1_t1");
      trip.setRoute(route);
      tripStatus.setActiveTrip(trip);

      StopBean stop = new StopBean();
      stop.setId("1_s3");
      tripStatus.setNextStop(stop);
      tripStatus.setNextStopTimeOffset(10 * 60);
    }

    ListBean<VehicleStatusBean> bean = new ListBean<VehicleStatusBean>();
    bean.setList(vehicles);
    Mockito.when(_service.getAllVehiclesForAgency("1", now)).thenReturn(bean);
    ListBean<TripDetailsBean> beans = new ListBean<>();
    beans.setList(new ArrayList<>());
    Mockito.when(_service.getTripsForAgency(any())).thenReturn(beans);
    _action.setId("1");
    _action.setTime(new Date(now));

    _action.show();

    ResponseBean model = _action.getModel();
    FeedMessage feed = (FeedMessage) model.getData();
    assertEquals(now / 1000, feed.getHeader().getTimestamp());
    assertEquals(2, feed.getEntityCount());

    {
      FeedEntity entity = feed.getEntity(0);
      assertEquals("1_t0_" + now, entity.getId());
      TripUpdate tripUpdate = entity.getTripUpdate();
      assertEquals("t0", tripUpdate.getTrip().getTripId());
      assertEquals("r1", tripUpdate.getTrip().getRouteId());
      assertEquals("v1", tripUpdate.getVehicle().getId());
      assertEquals(v1Timestamp/1000, tripUpdate.getTimestamp());
      assertEquals(120, tripUpdate.getDelay());
      assertEquals(1, tripUpdate.getStopTimeUpdateCount());
      StopTimeUpdate stopTimeUpdate = tripUpdate.getStopTimeUpdate(0);
      assertEquals("s2", stopTimeUpdate.getStopId());
      assertEquals(now / 1000 + 5 * 60, stopTimeUpdate.getDeparture().getTime());
    }
    {
      FeedEntity entity = feed.getEntity(1);
      assertEquals("1_t1_" + now, entity.getId());
      TripUpdate tripUpdate = entity.getTripUpdate();
      assertEquals("t1", tripUpdate.getTrip().getTripId());
      assertEquals("r1", tripUpdate.getTrip().getRouteId());
      assertEquals("v2", tripUpdate.getVehicle().getId());
      assertEquals(v2Timestamp/1000, tripUpdate.getTimestamp());
      assertEquals(180, tripUpdate.getDelay());
      assertEquals(1, tripUpdate.getStopTimeUpdateCount());
      StopTimeUpdate stopTimeUpdate = tripUpdate.getStopTimeUpdate(0);
      assertEquals("s3", stopTimeUpdate.getStopId());
      assertEquals(now / 1000 + 10 * 60,
          stopTimeUpdate.getDeparture().getTime());
    }
  }
}
