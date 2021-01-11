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
package org.onebusaway.api.actions.api.where;


import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyMapOf;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.onebusaway.api.actions.siri.StopPointsV2Action;
import org.onebusaway.api.actions.siri.impl.RealtimeServiceV2Impl;
import org.onebusaway.api.actions.siri.impl.SiriSupportV2.Filters;
import org.onebusaway.api.actions.siri.model.DetailLevel;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data.model.*;
import org.onebusaway.transit_data.model.RouteBean.Builder;
import org.onebusaway.transit_data.model.schedule.StopTimeBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.impl.ExtendedCalendarServiceImpl;
import org.onebusaway.transit_data_federation.impl.blocks.BlockIndexFactoryServiceImpl;
import org.onebusaway.transit_data_federation.impl.blocks.BlockIndexServiceImpl;
import org.onebusaway.transit_data_federation.impl.narrative.NarrativeServiceImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.*;
import org.onebusaway.transit_data_federation.model.narrative.AgencyNarrative;
import org.onebusaway.transit_data_federation.model.narrative.TripNarrative;
import org.onebusaway.transit_data_federation.services.beans.RouteScheduleBeanService;
import org.onebusaway.transit_data_federation.services.blocks.BlockTripIndex;
import org.onebusaway.transit_data_federation.services.transit_graph.*;
import org.onebusaway.transit_data_federation.siri.SiriXmlSerializerV2;
import org.onebusaway.util.services.configuration.ConfigurationService;

///THIS TEST IS INCOMPLETE
@RunWith(MockitoJUnitRunner.class)
public class ScheduleForRouteActionTest {

    private static final String AGENCY_ID = "1"; // UnitTestingSupport assumes this
    private static final String ROUTE_ID = "TLINK";
    private static final Date DATE = new Date();

    private NarrativeServiceImpl narrativeService = null;
    private BlockIndexServiceImpl blockIndexService = null;
    private ExtendedCalendarServiceImpl calendarService = null;
    private TransitGraphDao dao = null;
    private BlockIndexFactoryServiceImpl factory = null;
    private BlockTripIndex blockIndexWeekday = null;
    private BlockTripIndex blockIndexSaturday = null;
    private BlockTripIndex blockIndexSunday = null;


    @Mock
    private TransitDataService transitDataService;

    @Mock
    private RouteScheduleBeanService routeScheduleBeanService;

    @Mock
    private ConfigurationService configurationService;

    @InjectMocks
    private ScheduleForRouteAction action;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse servletResponse;

    RouteBean routeBean;
    List<RouteBean> routes;
    StopBean stopBean;
    List<StopBean> stops;
    StopGroupBean stopGroup;
    NameBean stopGroupName;
    List<String> stopIds;
    List<StopGroupBean> stopGroups;
    StopGroupingBean stopGrouping;
    List<StopGroupingBean> stopGroupings;
    StopsForRouteBean stopsForRouteBean;

//THIS TEST IS INCOMPLETE
    @Ignore
    public void testLineRef() throws Exception {

        AgencyAndId routeId = new AgencyAndId(AGENCY_ID,ROUTE_ID);
        RouteScheduleBean routeScheduleBean = createRouteScheduleBean();
        when(routeScheduleBeanService.getScheduledArrivalsForDate(
                routeId ,new ServiceDate(DATE)))
                .thenReturn(routeScheduleBean);

        action.setId(routeId.toString());
        action.setDate(DATE);
        DefaultHttpHeaders header = action.show();

        System.out.println(header);

        assertTrue("Result XML does not match expected", header!=null);
    }



    /**
     * Schedule info for a route.  Inspired by StopRouteScheduleBean.
     *
     * Ultimate goal of:
     *   "entry": {
     *       "routeId": "40_100479",
     *       "serviceIds": ["SERVICEIDVALUE1","SERVICEIDVALUE2"],
     *       "scheduleDate": 1609315200,
     *       "stopTripGroupings": [
     *         {
     *           "directionId": 0,
     *           "tripHeadsign": "University of Washington Station",
     *           "stopIds": ["STOPID1", "STOPID2"],
     *           "tripIds": ["TRIPID1", "TRIPID2"]
     *         },
     *         {
     *           "directionId": 1,
     *           "tripHeadsign": "Angle Lake Station",
     *           "stopIds": ["STOPID2", "STOPID3"],
     *           "tripIds": ["TRIPID3", "TRIPID4"]
     *         }
     *       ]
     *     },
     *     "references": {
     *       "agencies": [.....],
     *       "routes": [.....],
     *       "situations": [.....],
     *       "stops": [.....],
     *       "trips": [.....]
     *     },
     */

    private RouteScheduleBean createRouteScheduleBean() {

        RouteScheduleBean routeScheduleBean = new RouteScheduleBean();

        List<AgencyBean> agencies = new ArrayList<>();
        AgencyBean agencyBean = new AgencyBean();
        agencyBean.setId(AGENCY_ID);
        agencies.add(agencyBean);


        List<RouteBean> routes = new ArrayList<>();
        Builder routeBeanBuilder = RouteBean.builder();
        routeBeanBuilder.setAgency(agencyBean);
        routeBeanBuilder.setId(ROUTE_ID);
        RouteBean route  = routeBeanBuilder.create();
        routes.add(route);




        List<TripBean> trips = new ArrayList<>();

        BlockEntryImpl b1 = block("TacL1000");
        TripBean t1 = createTripBean("TacL1000", "WD_TL", 0);
        t1.setDirectionId("0");
        t1.setRoute(route);
        t1.setTripHeadsign("Theater District Station");
        trips.add(t1);

        BlockEntryImpl b2 = block("TacL1001");
        TripBean t2 = createTripBean("TacL1001", "WD_TL", 0);
        t2.setDirectionId("1");
        t2.setRoute(route);
        t2.setTripHeadsign("Tacoma Dome Station");
        trips.add(t2);

        BlockEntryImpl b3 = block("TacL6000");
        TripBean t3 = createTripBean("TacL6000", "SA", 0);
        t3.setDirectionId("0");
        t3.setRoute(route);
        t3.setTripHeadsign("Theater District Station");
        trips.add(t3);

        BlockEntryImpl b4 = block("TacL6001");
        TripBean t4 = createTripBean("TacL6001", "SA", 0);
        t4.setDirectionId("1");
        t4.setRoute(route);
        t4.setTripHeadsign("Tacoma Dome Station");
        trips.add(t4);

        BlockEntryImpl b5 = block("TacL7000");
        TripBean t5 = createTripBean("TacL7000", "SU", 0);
        t5.setDirectionId("1");
        t5.setRoute(route);
        t5.setTripHeadsign("Theater District Station");
        trips.add(t5);

        BlockEntryImpl b6 = block("TacL7001");
        TripBean t6 = createTripBean("TacL7001", "SU", 0);
        t6.setDirectionId("1");
        t6.setRoute(route);
        t6.setTripHeadsign("Tacoma Dome Station");
        trips.add(t6);


        List<StopBean> stops = new ArrayList<>();
        StopBean std = createStopBean("TL_TD", 47.239868,-122.428118);
        StopBean s25 = createStopBean("TL_25", 47.239081,-122.434202);
        StopBean sus = createStopBean("TL_US", 47.244865,-122.436623);
        StopBean scc = createStopBean("TL_CC", 47.249496,-122.438552);
        stops.add(std);
        stops.add(s25);
        stops.add(sus);
        stops.add(scc);

        List<StopTimeInstanceBean> stopTimeInstances = new ArrayList<>();
        StopTimeInstanceBean st_1_1 = createStopTimeInstanceBean(AGENCY_ID, std, t1, time(5, 0, 0), time(5, 0,0 ), 0);
        StopTimeInstanceBean st_1_2 = createStopTimeInstanceBean(AGENCY_ID, s25, t1, time(5, 2, 0), time(5, 2,0 ), 0);
        StopTimeInstanceBean st_1_3 = createStopTimeInstanceBean(AGENCY_ID, sus, t1, time(5, 4, 0), time(5, 4,0 ), 0);
        StopTimeInstanceBean st_1_4 = createStopTimeInstanceBean(AGENCY_ID, scc, t1, time(5, 6, 0), time(5, 6,0 ), 0);
        stopTimeInstances.add(st_1_1);
        stopTimeInstances.add(st_1_2);
        stopTimeInstances.add(st_1_3);
        stopTimeInstances.add(st_1_4);

        StopTimeInstanceBean st_2_1 = createStopTimeInstanceBean(AGENCY_ID, scc, t2, time(5, 16, 0), time(5, 16,0 ), 0);
        StopTimeInstanceBean st_2_2 = createStopTimeInstanceBean(AGENCY_ID, sus, t2, time(5, 18, 0), time(5, 18,0 ), 0);
        StopTimeInstanceBean st_2_3 = createStopTimeInstanceBean(AGENCY_ID, s25, t2, time(5, 20, 0), time(5, 20,0 ), 0);
        StopTimeInstanceBean st_2_4 = createStopTimeInstanceBean(AGENCY_ID, std, t2, time(5, 22, 0), time(5, 22,0 ), 0);
        stopTimeInstances.add(st_2_1);
        stopTimeInstances.add(st_2_2);
        stopTimeInstances.add(st_2_3);
        stopTimeInstances.add(st_2_4);

        StopTimeInstanceBean st_3_1 = createStopTimeInstanceBean(AGENCY_ID, std, t3, time(6, 0, 0), time(5, 0,0 ), 0);
        StopTimeInstanceBean st_3_2 = createStopTimeInstanceBean(AGENCY_ID, s25, t3, time(6, 2, 0), time(5, 2,0 ), 0);
        StopTimeInstanceBean st_3_3 = createStopTimeInstanceBean(AGENCY_ID, sus, t3, time(6, 4, 0), time(5, 4,0 ), 0);
        StopTimeInstanceBean st_3_4 = createStopTimeInstanceBean(AGENCY_ID, scc, t3, time(6, 6, 0), time(5, 6,0 ), 0);
        stopTimeInstances.add(st_3_1);
        stopTimeInstances.add(st_3_2);
        stopTimeInstances.add(st_3_3);
        stopTimeInstances.add(st_3_4);

        StopTimeInstanceBean st_4_1 = createStopTimeInstanceBean(AGENCY_ID, scc, t4, time(6, 16, 0), time(5, 16,0 ), 0);
        StopTimeInstanceBean st_4_2 = createStopTimeInstanceBean(AGENCY_ID, sus, t4, time(6, 18, 0), time(5, 18,0 ), 0);
        StopTimeInstanceBean st_4_3 = createStopTimeInstanceBean(AGENCY_ID, s25, t4, time(6, 20, 0), time(5, 20,0 ), 0);
        StopTimeInstanceBean st_4_4 = createStopTimeInstanceBean(AGENCY_ID, std, t4, time(6, 22, 0), time(5, 22,0 ), 0);
        stopTimeInstances.add(st_4_1);
        stopTimeInstances.add(st_4_2);
        stopTimeInstances.add(st_4_3);
        stopTimeInstances.add(st_4_4);

        StopTimeInstanceBean st_5_1 = createStopTimeInstanceBean(AGENCY_ID, std, t5, time(7, 0, 0), time(5, 0,0 ), 0);
        StopTimeInstanceBean st_5_2 = createStopTimeInstanceBean(AGENCY_ID, s25, t5, time(7, 2, 0), time(5, 2,0 ), 0);
        StopTimeInstanceBean st_5_3 = createStopTimeInstanceBean(AGENCY_ID, sus, t5, time(7, 4, 0), time(5, 4,0 ), 0);
        StopTimeInstanceBean st_5_4 = createStopTimeInstanceBean(AGENCY_ID, scc, t5, time(7, 6, 0), time(5, 6,0 ), 0);
        stopTimeInstances.add(st_5_1);
        stopTimeInstances.add(st_5_2);
        stopTimeInstances.add(st_5_3);
        stopTimeInstances.add(st_5_4);

        StopTimeInstanceBean st_6_1 = createStopTimeInstanceBean(AGENCY_ID, scc, t6, time(7, 16, 0), time(5, 16,0 ), 0);
        StopTimeInstanceBean st_6_2 = createStopTimeInstanceBean(AGENCY_ID, sus, t6, time(7, 18, 0), time(5, 18,0 ), 0);
        StopTimeInstanceBean st_6_3 = createStopTimeInstanceBean(AGENCY_ID, s25, t6, time(7, 20, 0), time(5, 20,0 ), 0);
        StopTimeInstanceBean st_6_4 = createStopTimeInstanceBean(AGENCY_ID, std, t6, time(7, 22, 0), time(5, 22,0 ), 0);
        stopTimeInstances.add(st_6_1);
        stopTimeInstances.add(st_6_2);
        stopTimeInstances.add(st_6_3);
        stopTimeInstances.add(st_6_4);

        List<AgencyAndId> serviceIds = new ArrayList<>();
        AgencyAndId serviceIdActivationWeekday = new AgencyAndId(AGENCY_ID,"WD_TL");
        AgencyAndId serviceIdActivationSaturday = new AgencyAndId(AGENCY_ID,"SA");
        AgencyAndId serviceIdActivationSunday = new AgencyAndId(AGENCY_ID,"SU");
        serviceIds.add(serviceIdActivationSaturday);
        serviceIds.add(serviceIdActivationSunday);
        serviceIds.add(serviceIdActivationWeekday);


        routeScheduleBean.setRouteId(new AgencyAndId(AGENCY_ID,ROUTE_ID));
        //the schedule date is wrong
        routeScheduleBean.setScheduleDate(new ServiceDate(new Date()));

        //never did stopTripDirections
        //routeScheduleBean.getStopTripDirections().addAll(headsignToBeanMap.values());
        routeScheduleBean.getAgencies().addAll(agencies);
        routeScheduleBean.getRoutes().addAll(routes);
        routeScheduleBean.getTrips().addAll(trips);
        routeScheduleBean.getStops().addAll(stops);
        routeScheduleBean.getStopTimes().addAll(stopTimeInstances);

        return routeScheduleBean;
    }

    private TripBean createTripBean(String id, String serviceId, int totalTripDistance){
        TripBean tripBean = new TripBean();
        tripBean.setId(id);
        tripBean.setServiceId(serviceId);
        tripBean.setTotalTripDistance(totalTripDistance);
        return tripBean;
    }

    private StopBean createStopBean(String id, Double lat, Double lon){
        StopBean stopBean = new StopBean();
        stopBean.setId(id);
        stopBean.setLat(lat);
        stopBean.setLon(lon);
        return stopBean;
    }

    //This should really have more fields filled out in the stopTimeInstanceBean
    private StopTimeInstanceBean createStopTimeInstanceBean (String id, StopBean stop, TripBean trip,long arrival, long departure, long shapeDist){
        StopTimeInstanceBean stopTimeInstanceBean = new StopTimeInstanceBean();
        stopTimeInstanceBean.setTripId(id);
        stopTimeInstanceBean.setArrivalTime(arrival);
        stopTimeInstanceBean.setDepartureTime(departure);
        return stopTimeInstanceBean;
    }
}
