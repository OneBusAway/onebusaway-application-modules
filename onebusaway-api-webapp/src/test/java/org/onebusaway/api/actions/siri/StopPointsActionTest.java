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
package org.onebusaway.api.actions.siri;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.onebusaway.api.actions.siri.SiriAction.STOP_POINTS_DETAIL_LEVEL;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.onebusaway.api.actions.siri.impl.RealtimeServiceV2Impl;
import org.onebusaway.api.actions.siri.impl.SiriSupportV2.Filters;
import org.onebusaway.api.actions.siri.model.DetailLevel;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.NameBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.RouteBean.Builder;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopGroupBean;
import org.onebusaway.transit_data.model.StopGroupingBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.siri.SiriXmlSerializerV2;
import org.onebusaway.util.services.configuration.ConfigurationService;

import uk.org.siri.siri_2.AnnotatedStopPointStructure;
import uk.org.siri.siri_2.DirectionRefStructure;
import uk.org.siri.siri_2.LineDirectionStructure;
import uk.org.siri.siri_2.LineRefStructure;
import uk.org.siri.siri_2.LocationStructure;
import uk.org.siri.siri_2.NaturalLanguageStringStructure;
import uk.org.siri.siri_2.StopPointRefStructure;

@RunWith(MockitoJUnitRunner.class)
public class StopPointsActionTest {

  private static final long serialVersionUID = 1L;

  @Mock
  private RealtimeServiceV2Impl realtimeService;
  
  @Mock
  private TransitDataService transitDataService;
  
  @Mock
  private ConfigurationService configurationService;
  
  @InjectMocks
  private StopPointsV2Action action;

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
  
  
  @Before
  public void initialize() throws Exception{
    
    // Agencies
    Map<String, List<CoordinateBounds>> agencies = new  HashMap<String, List<CoordinateBounds>>();
    agencies.put("1", new ArrayList<CoordinateBounds>(Arrays.asList(new CoordinateBounds(47.410813,-122.038662,47.810813,-122.638662))));
    agencies.put("3", new ArrayList<CoordinateBounds>(Arrays.asList(new CoordinateBounds(0.0,0.0,0.0,0.0))));
    agencies.put("40", new ArrayList<CoordinateBounds>(Arrays.asList(new CoordinateBounds(47.510813,-122.138662,47.710813,-122.538662))));
        
    // Route Bean
    Builder routeBuilder = RouteBean.builder();
    routeBuilder.setAgency(new AgencyBean());
    routeBuilder.setId("1_100194");
    routeBean = routeBuilder.create();
    
    // Route Bean List
    routes = new ArrayList<RouteBean>(1);
    routes.add(routeBean);
    
    //Stop Bean
    stopBean = new StopBean();
    stopBean.setId("1_430");
    stopBean.setName("3rd Ave & Pine St");
    stopBean.setLon(-122.338662);
    stopBean.setLat(47.610813);
    stopBean.setRoutes(routes);
    
    //Stop Bean List
    stops = new ArrayList<StopBean>(1);
    stops.add(stopBean);
    
    //Stop Group
    stopIds = new ArrayList<String>(1);
    stopIds.add(stopBean.getId());
    stopGroupName = new NameBean("destination", "Destination");
    
    stopGroup = new StopGroupBean();
    stopGroup.setId("0");
    stopGroup.setStopIds(stopIds);
    stopGroup.setName(stopGroupName);
    
    //Stop Group List
    stopGroups = new ArrayList<StopGroupBean>(1); 
    stopGroups.add(stopGroup);
    
    //Stop Grouping
    stopGrouping = new StopGroupingBean();
    stopGrouping.setStopGroups(stopGroups);
    
    //Stop Grouping List
    List<StopGroupingBean> stopGroupings = new ArrayList<StopGroupingBean>(1);
    stopGroupings.add(stopGrouping);
    
    //Stops For Route
    stopsForRouteBean =  new StopsForRouteBean();
    stopsForRouteBean.setRoute(routeBean);
    stopsForRouteBean.setStopGroupings(stopGroupings);
    stopsForRouteBean.setStops(stops);
    

  //LineDirectionStructure
  LineDirectionStructure lds = new LineDirectionStructure();
  DirectionRefStructure drs = new DirectionRefStructure();
  LineRefStructure lrs = new LineRefStructure();
    
  lds.setDirectionRef(drs);
  lds.setLineRef(lrs);
  drs.setValue("0");
  lrs.setValue("1_100194");
 
  //Location Structure
  LocationStructure ls =  new LocationStructure();
  BigDecimal lat = new BigDecimal(47.610813);
  BigDecimal lon = new BigDecimal(-122.338662);
   
  ls.setLongitude(lon.setScale(6, BigDecimal.ROUND_HALF_DOWN));
  ls.setLatitude(lat.setScale(6, BigDecimal.ROUND_HALF_DOWN));
  
  //StopNames
  NaturalLanguageStringStructure stopName = new NaturalLanguageStringStructure();
  stopName.setValue("3rd Ave & Pine St");
  List<NaturalLanguageStringStructure> stopNames = new ArrayList<NaturalLanguageStringStructure>();
  stopNames.add(stopName);
  
  //StopPointRef
  StopPointRefStructure stopPointRef = new StopPointRefStructure();
  stopPointRef.setValue("1_430");
  
  //Monitored
  Boolean monitored = true; 
  
  //AnnotatedStopPointStructure
  AnnotatedStopPointStructure mockStopPoint = new AnnotatedStopPointStructure();
  mockStopPoint.setLines(new AnnotatedStopPointStructure.Lines());
  mockStopPoint.getLines().getLineRefOrLineDirection().add(lds);
  mockStopPoint.setLocation(ls);
  mockStopPoint.getStopName().add(stopName);
  mockStopPoint.setStopPointRef(stopPointRef);
  mockStopPoint.setMonitored(monitored);
    
    
  List<AnnotatedStopPointStructure> mockStopPoints = new ArrayList<AnnotatedStopPointStructure>(1);
  mockStopPoints.add(mockStopPoint);
    
  Map<Boolean, List<AnnotatedStopPointStructure>> annotatedStopPointMap = new HashMap<Boolean, List<AnnotatedStopPointStructure>>();
  annotatedStopPointMap.put(true, mockStopPoints);

  when(realtimeService.getAnnotatedStopPointStructures(anyListOf(String.class), anyListOf(AgencyAndId.class), any(DetailLevel.class), anyLong(), anyMapOf(Filters.class, String.class))).thenReturn(annotatedStopPointMap);
    
  // XML Serializer
  SiriXmlSerializerV2 serializer = new SiriXmlSerializerV2();
  when(realtimeService.getSiriXmlSerializer()).thenReturn(serializer );

  when(transitDataService.getRouteForId("1_430")).thenReturn(routeBean);
  lenient().when(transitDataService.getStopsForRoute("1_430")).thenReturn(stopsForRouteBean);
  lenient().when(transitDataService.stopHasUpcomingScheduledService(anyString(), anyLong(), anyString(), anyString(), anyString())).thenReturn(true);
  lenient().when(transitDataService.getAgencyIdsWithCoverageArea()).thenReturn(agencies);
  
  }
  
  
  @Test
  public void testLineRef() throws Exception {
    
    
    when(request.getParameter(eq("LineRef"))).thenReturn("430");

    action.setServletRequest(request);
    action.setServletResponse(servletResponse);
    action.index();
    
    String monitoring = action.getSiri();
    System.out.println(monitoring);
    
    assertTrue("Result XML does not match expected", monitoring.matches("(?s).*<StopPointsDelivery><ResponseTimestamp>.+</ResponseTimestamp><ValidUntil>.+</ValidUntil><AnnotatedStopPointRef><StopPointRef>.+</StopPointRef><Monitored>true</Monitored><StopName>.+</StopName><Lines><LineDirection><LineRef>.+</LineRef><DirectionRef>(0|1)</DirectionRef></LineDirection></Lines><Location><Longitude>\\-[0-9]{1,3}?\\.[0-9]+</Longitude><Latitude>[0-9]{1,2}?\\.[0-9]+</Latitude></Location></AnnotatedStopPointRef><Extensions><UpcomingScheduledService>true</UpcomingScheduledService></Extensions></StopPointsDelivery></Siri>.*"));
}
  
  
  @Test
  public void testDetailLevelCase() throws Exception {
    
    when(request.getParameter(eq("LineRef"))).thenReturn("430");
    when(request.getParameter(eq(STOP_POINTS_DETAIL_LEVEL))).thenReturn("Calls");

    action.setServletRequest(request);
    action.setServletResponse(servletResponse);
    action.index();
    
    String monitoring = action.getSiri();
    System.out.println(monitoring);
    
    assertTrue("Result XML does not match expected", monitoring.matches("(?s).*<StopPointsDelivery><ResponseTimestamp>.+</ResponseTimestamp><ValidUntil>.+</ValidUntil><AnnotatedStopPointRef><StopPointRef>.+</StopPointRef><Monitored>true</Monitored><StopName>.+</StopName><Lines><LineDirection><LineRef>.+</LineRef><DirectionRef>(0|1)</DirectionRef></LineDirection></Lines><Location><Longitude>.+</Longitude><Latitude>.+</Latitude></Location></AnnotatedStopPointRef><Extensions><UpcomingScheduledService>true</UpcomingScheduledService></Extensions></StopPointsDelivery></Siri>.*"));
  }
}
