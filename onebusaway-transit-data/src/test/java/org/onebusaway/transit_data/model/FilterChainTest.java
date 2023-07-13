/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data.model;

import org.junit.Test;
import org.onebusaway.transit_data.model.trips.TripBean;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class FilterChainTest {

  @Test
  public void matchesRouteTypeFilter() {
    FilterChain chain = new FilterChain();

    ArrivalAndDepartureBean adBean1 = createADBeanWithType(null, 1, null);
    ArrivalAndDepartureBean adBean3 = createADBeanWithType(null, 3, null);
    // empty chain should always match
    assertTrue(chain.matches(adBean1));
    assertTrue(chain.matches(adBean3));

    ArrivalAndDepartureFilterByRouteType routeTypeFilter1 = new ArrivalAndDepartureFilterByRouteType("1");
    chain.add(routeTypeFilter1);
    
    assertTrue(chain.matches(adBean1));
    assertFalse(chain.matches(adBean3));
  }

  @Test
  public void matchesRouteTypeAndRealtimeFilter() {
    FilterChain chain = new FilterChain();

    ArrivalAndDepartureBean adBean1 = createADBeanWithType("1_1", 1, null);
    ArrivalAndDepartureBean adBean1RT = createADBeanWithType("1_2", 1, "1_1");
    ArrivalAndDepartureBean adBean1RT3 = createADBeanWithType("3_3", 1, "3_1");
    ArrivalAndDepartureBean adBean1RTCancelled = createADBeanWithType("1_4", 1, "1_4", true);
    ArrivalAndDepartureBean adBean2 = createADBeanWithType("2_1", 2, null);
    ArrivalAndDepartureBean adBean2RT = createADBeanWithType("2_2", 2, "2_2");
    ArrivalAndDepartureBean adBean3 = createADBeanWithType("3_1",3, null);
    ArrivalAndDepartureBean adBean3RT = createADBeanWithType("3_2",3, "3_3");
    // empty chain should always match
    assertTrue(chain.matches(adBean1));
    assertTrue(chain.matches(adBean1RT));
    assertTrue(chain.matches(adBean1RT3));
    assertTrue(chain.matches(adBean1RTCancelled));
    assertTrue(chain.matches(adBean2));
    assertTrue(chain.matches(adBean2RT));
    assertTrue(chain.matches(adBean3));
    assertTrue(chain.matches(adBean3RT));

    ArrivalAndDepartureFilterByRouteType routeTypeFilter1 = new ArrivalAndDepartureFilterByRouteType("1,2");
    chain.add(routeTypeFilter1);
    List<String> realtimeAgencies = new ArrayList<>();
    realtimeAgencies.add("1");
    ArrivalAndDepartureFilterByRealtime realtimeFilter1 = new ArrivalAndDepartureFilterByRealtime(realtimeAgencies);
    chain.add(realtimeFilter1);

    assertFalse(chain.matches(adBean1));
    assertTrue(chain.matches(adBean1RT));
    assertTrue(chain.matches(adBean1RT3));
    // cancelled should still show, it's a real-time status
    assertTrue(chain.matches(adBean1RTCancelled));
    // agency 2 doesn't have a real-time filter
    assertTrue(chain.matches(adBean2));
    assertTrue(chain.matches(adBean2RT));
    // route_type 3 is filtered out
    assertFalse(chain.matches(adBean3));
    assertFalse(chain.matches(adBean3RT));
  }


  @Test
  public void matchesStopFilter() {
    FilterChain chain = new FilterChain();
    StopBean stopBean1 = createStopBeanWithType(1);
    StopBean stopBean3 = createStopBeanWithType(3);
    // empty chain should always match
    assertTrue(chain.matches(stopBean1));
    assertTrue(chain.matches(stopBean3));

    StopFilter stopFilter1 = new StopFilterByRouteType("1");
    chain.add(stopFilter1);
    assertTrue(chain.matches(stopBean1));
    assertFalse(chain.matches(stopBean3));
  }

  private ArrivalAndDepartureBean createADBeanWithType(String tripId, int routeType, String vehicleId) {
    return createADBeanWithType(tripId, routeType, vehicleId, false);
  }
  private ArrivalAndDepartureBean createADBeanWithType(String tripId, int routeType, String vehicleId, boolean cancelled) {
    ArrivalAndDepartureBean adBean = new ArrivalAndDepartureBean();
    StopBean stopBean = new StopBean();
    adBean.setStop(stopBean);
    stopBean.setRoutes(new ArrayList<>());
    RouteBean.Builder route1Builder = RouteBean.builder();
    route1Builder.setType(routeType);
    stopBean.getRoutes().add(route1Builder.create());

    if (tripId != null) {
      adBean.setTrip(new TripBean());
      adBean.getTrip().setId(tripId);
    }

    if (vehicleId != null) {
      adBean.setPredicted(true);
      long now = System.currentTimeMillis();
      adBean.setPredictedArrivalTime(now + 5 * 1000);
      adBean.setPredictedDepartureTime(now + 5 * 1000);
    }
    if (cancelled) {
      adBean.setStatus(TransitDataConstants.STATUS_CANCELED);
      adBean.setPredicted(false);
      adBean.setPredictedArrivalTime(-1);
      adBean.setPredictedDepartureTime(-1);
    }
    return adBean;

  }


  private StopBean createStopBeanWithType(int routeType) {
    StopBean stopBean = new StopBean();
    RouteBean.Builder routeBuilder = RouteBean.builder();
    routeBuilder.setType(routeType);
    stopBean.setRoutes(new ArrayList<>());
    stopBean.getRoutes().add(routeBuilder.create());
    return stopBean;
  }
}