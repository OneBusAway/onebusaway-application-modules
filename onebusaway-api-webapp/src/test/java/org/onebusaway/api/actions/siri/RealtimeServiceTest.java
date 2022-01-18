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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.onebusaway.api.actions.siri.impl.RealtimeServiceV2Impl;
import org.onebusaway.api.actions.siri.impl.SiriSupportV2.Filters;
import org.onebusaway.api.actions.siri.model.DetailLevel;
import org.onebusaway.api.actions.siri.service.RealtimeServiceV2;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.presentation.services.realtime.PresentationService;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.NameBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.RouteBean.Builder;
import org.onebusaway.transit_data.model.SearchQueryBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopGroupBean;
import org.onebusaway.transit_data.model.StopGroupingBean;
import org.onebusaway.transit_data.model.StopsBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.onebusaway.util.services.configuration.ConfigurationService;

import uk.org.siri.siri_2.AnnotatedStopPointStructure;
import uk.org.siri.siri_2.DirectionRefStructure;
import uk.org.siri.siri_2.LineDirectionStructure;
import uk.org.siri.siri_2.LineRefStructure;
import uk.org.siri.siri_2.LocationStructure;
import uk.org.siri.siri_2.NaturalLanguageStringStructure;
import uk.org.siri.siri_2.StopPointRefStructure;

@RunWith(MockitoJUnitRunner.class)
public class RealtimeServiceTest {

  private static final long serialVersionUID = 1L;
  
  @InjectMocks
  private StopPointsV2Action action;
  
  @Mock
  private RealtimeServiceV2 realtimeService2;
  
  @InjectMocks
  private RealtimeServiceV2Impl realtimeService;
  
  @Mock
  private PresentationService presentationService;
  
  @Mock
  private TransitDataService transitDataService;
  
  @Mock
  private ConfigurationService configurationService;
  
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
  public void initialize(){
    
    // Agency Bean
    AgencyBean agency = new AgencyBean();
    agency.setId("1");
    
    // Route Bean
    Builder routeBuilder = RouteBean.builder();
    routeBuilder.setAgency(agency);
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
  }
  
  @Test
  public void testStopPointsByRoute() throws Exception {
    
    lenient().when(transitDataService.getRouteForId("1_100194")).thenReturn(routeBean);
    when(transitDataService.getStopsForRoute("1_100194")).thenReturn(stopsForRouteBean);
    when(transitDataService.stopHasUpcomingScheduledService(anyString(), anyLong(), anyString(), anyString(), anyString())).thenReturn(true);
    
    LineDirectionStructure lds = new LineDirectionStructure();
    DirectionRefStructure drs = new DirectionRefStructure();
    LineRefStructure lrs = new LineRefStructure();
    
    lds.setDirectionRef(drs);
    lds.setLineRef(lrs);
    drs.setValue("0");
    lrs.setValue("1_100194");
    
    LocationStructure ls =  new LocationStructure();
    BigDecimal lat = new BigDecimal(47.610813);
    BigDecimal lon = new BigDecimal(-122.338662);
   
    ls.setLatitude(lat.setScale(6, BigDecimal.ROUND_HALF_DOWN));
    ls.setLongitude(lon.setScale(6, BigDecimal.ROUND_HALF_DOWN));
    
    NaturalLanguageStringStructure stopName = new NaturalLanguageStringStructure();
    stopName.setValue("3rd Ave & Pine St");
    List<NaturalLanguageStringStructure> stopNames = new ArrayList<NaturalLanguageStringStructure>();
    stopNames.add(stopName);
    
    StopPointRefStructure stopPointRef = new StopPointRefStructure();
    stopPointRef.setValue("1_430");
    
    Boolean monitored = true; 
    
    AnnotatedStopPointStructure mockStopPoint = new AnnotatedStopPointStructure();
    mockStopPoint.setLines(new AnnotatedStopPointStructure.Lines());
    mockStopPoint.getLines().getLineRefOrLineDirection().add(lds);
    mockStopPoint.setLocation(ls);
    mockStopPoint.getStopName().add(stopName);
    mockStopPoint.setStopPointRef(stopPointRef);
    mockStopPoint.setMonitored(monitored);
    
        
    // REALTIME ARGUMENTS
  
    // Agency Ids
    List<String> agencyIds = new ArrayList<String>();
    String agencyId = "1";
    agencyIds.add(agencyId);
    
    //  Route Ids
    List<AgencyAndId> routeIds = new ArrayList<AgencyAndId>();
    AgencyAndId routeId = AgencyAndIdLibrary.convertFromString("1_100194");
    routeIds.add(routeId);
      
    //  Detail Level
    DetailLevel detailLevel = DetailLevel.NORMAL;
  
    //  Time
    long time = System.currentTimeMillis();
    
    // Filters
    Map<Filters, String> filters = new HashMap<Filters, String>();
    
    Map<Boolean, List<AnnotatedStopPointStructure>> actualResult = realtimeService.getAnnotatedStopPointStructures(agencyIds, routeIds, detailLevel, time, filters);
    AnnotatedStopPointStructure actualStopPoint = actualResult.get(true).get(0);
    
    assertTrue(isEqual(mockStopPoint, actualStopPoint));

  }
  
  @Test
  public void testStopPointsByBounds() throws Exception {
  // MOCKS
  
  // Coordinate Bounds
  CoordinateBounds bounds = new CoordinateBounds(
    Double.parseDouble("47.612813"), 
    Double.parseDouble("-122.339662"),
    Double.parseDouble("47.608813"),
    Double.parseDouble("-122.337662"));
  
    // Stops For Bounds
    StopsBean stopsBean = new StopsBean();
    stopsBean.setStops(stops);
    
    lenient().when(transitDataService.getRouteForId("1_100194")).thenReturn(routeBean);
    
    when(transitDataService.getStops(any(SearchQueryBean.class))).thenReturn(stopsBean);

    when(transitDataService.getStopsForRoute("1_100194")).thenReturn(stopsForRouteBean);
    
    when(transitDataService.stopHasUpcomingScheduledService(anyString(), anyLong(), anyString(), anyString(), anyString())).thenReturn(true);
    
    
    // EXPECTED
    
    LineDirectionStructure lds = new LineDirectionStructure();
    DirectionRefStructure drs = new DirectionRefStructure();
    LineRefStructure lrs = new LineRefStructure();
    
    lds.setDirectionRef(drs);
    lds.setLineRef(lrs);
    drs.setValue("0");
    lrs.setValue("1_100194");
   
    LocationStructure ls =  new LocationStructure();
    BigDecimal lat = new BigDecimal(47.610813);
    BigDecimal lon = new BigDecimal(-122.338662);
   
    ls.setLatitude(lat.setScale(6, BigDecimal.ROUND_HALF_DOWN));
    ls.setLongitude(lon.setScale(6, BigDecimal.ROUND_HALF_DOWN));
    
    NaturalLanguageStringStructure stopName = new NaturalLanguageStringStructure();
    stopName.setValue("3rd Ave & Pine St");
    List<NaturalLanguageStringStructure> stopNames = new ArrayList<NaturalLanguageStringStructure>();
    stopNames.add(stopName);
    
    StopPointRefStructure stopPointRef = new StopPointRefStructure();
    stopPointRef.setValue("1_430");
    
    Boolean monitored = true;
        
    
    AnnotatedStopPointStructure mockStopPoint = new AnnotatedStopPointStructure();
    mockStopPoint.setLines(new AnnotatedStopPointStructure.Lines());
    mockStopPoint.getLines().getLineRefOrLineDirection().add(lds);
    mockStopPoint.setLocation(ls);
    mockStopPoint.getStopName().add(stopName);
    mockStopPoint.setStopPointRef(stopPointRef);
    mockStopPoint.setMonitored(monitored);
    
        
    // REALTIME ARGUMENTS
    
    // Agency Ids
    List<String> agencyIds = new ArrayList<String>();
    String agencyId = "1";
    agencyIds.add(agencyId);
  
    //  Route Ids
    List<AgencyAndId> routeIds = new ArrayList<AgencyAndId>();
    AgencyAndId routeId = AgencyAndIdLibrary.convertFromString("1_100194");
    routeIds.add(routeId);
      
    //  Detail Level
    DetailLevel detailLevel = DetailLevel.NORMAL;
  
    //  Time
    long time = System.currentTimeMillis();
    
    // Filters
    Map<Filters, String> filters = new HashMap<Filters, String>();
    
    Map<Boolean, List<AnnotatedStopPointStructure>> actualResult = realtimeService.getAnnotatedStopPointStructures(bounds, agencyIds, routeIds,detailLevel, time, filters);
    AnnotatedStopPointStructure actualStopPoint = actualResult.get(true).get(0);

    assertTrue(isEqual(mockStopPoint, actualStopPoint));

  }
  
  
  private boolean isEqual(AnnotatedStopPointStructure sp1, AnnotatedStopPointStructure sp2){
    boolean linesEq = EqualsBuilder.reflectionEquals(sp1.getLines().getLineRefOrLineDirection(), sp2.getLines().getLineRefOrLineDirection());
    boolean stopNameEq = EqualsBuilder.reflectionEquals(sp1.getStopName(), sp2.getStopName());
    boolean sprEq = EqualsBuilder.reflectionEquals(sp1.getStopPointRef(), sp2.getStopPointRef());
    boolean locationEq = EqualsBuilder.reflectionEquals(sp1.getLocation(), sp2.getLocation());
    boolean monEq = EqualsBuilder.reflectionEquals(sp1.isMonitored(), sp2.isMonitored());

    if(linesEq && stopNameEq && sprEq && locationEq && monEq)
      return true;
    
    return false;     
  }
}
