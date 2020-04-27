/**
 * Copyright (C) 2011 Metropolitan Transportation Authority
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
package org.onebusaway.enterprise.webapp.actions.m;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.onebusaway.presentation.services.realtime.RealtimeService;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.siri.SiriDistanceExtension;
import org.onebusaway.transit_data_federation.siri.SiriExtensionWrapper;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.onebusaway.enterprise.webapp.actions.m.model.RouteResult;
import org.onebusaway.enterprise.webapp.actions.m.model.StopResult;
import org.onebusaway.transit_data.model.NameBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.RouteBean.Builder;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopGroupBean;
import org.onebusaway.transit_data.model.StopGroupingBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.transit_data.model.service_alerts.NaturalLanguageStringBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;

import uk.org.siri.siri.DirectionRefStructure;
import uk.org.siri.siri.ExtensionsStructure;
import uk.org.siri.siri.LineRefStructure;
import uk.org.siri.siri.MonitoredCallStructure;
import uk.org.siri.siri.MonitoredStopVisitStructure;
import uk.org.siri.siri.MonitoredVehicleJourneyStructure;
import uk.org.siri.siri.NaturalLanguageStringStructure;

@RunWith(MockitoJUnitRunner.class)
public class SearchResultFactoryImplTest {

  private static final String TEST_DESCRIPTION = "Test description";
  private static final String TEST_DESCRIPTION2 = "Test description 2";
  private static final String TEST_SUMMARY = "Test summary";
  private static final String ROUTE_ID = "route id";
  private static final String TEST_DESTINATION_NAME = "destination name";
  private static final String TEST_STOP_ID = "test stop id";
  private static final String TEST_PRESENTABLE_DISTANCE = "test presentable distance";
  private static final long TEST_TIME = System.currentTimeMillis();

  @Mock
  private ConfigurationService _configurationService;

  @Mock
  private RealtimeService _realtimeService;

  @Mock
  private TransitDataService _transitDataService;

  // getRouteResult tests
  
  @Test
  public void testGetRouteResultServiceAlertWithNoDescriptionsOrSummaries() {
    RouteResult result = runGetRouteResult(createServiceAlerts(new String[] {},
        new String[] {}));
    Set<String> alerts = result.getServiceAlerts();
    assertEquals(1, alerts.size());
    assertEquals("(no description)", alerts.toArray()[0]);
    assertEquals("name not expected", ROUTE_ID, result.getId());
  }

  @Test
  public void testGetRouteResultServiceAlertWithDescriptionsOnly() {
    RouteResult result = runGetRouteResult(createServiceAlerts(new String[] {
        TEST_DESCRIPTION, TEST_DESCRIPTION2}, new String[] {TEST_SUMMARY}));
    Set<String> alerts = result.getServiceAlerts();
    assertEquals(3, alerts.size());
    String[] array = alerts.toArray(new String[] {});
    // array position is no longer guaranteed
    boolean found0 = false, found1 = false, found2 = false;
    for (String s : array) {
      if (TEST_DESCRIPTION.equals(s)) found0 = true;
      if (TEST_DESCRIPTION2.equals(s)) found1 = true;
      if (markup(TEST_SUMMARY).equals(s)) found2 = true;
    }
    assertTrue(found0);
    assertTrue(found1);
    assertTrue(found2);
    assertEquals("name not expected", ROUTE_ID, result.getId());
  }

  @Test
  public void testGetRouteResultServiceAlertWithSummariesOnly() {
    RouteResult result = runGetRouteResult(createServiceAlerts(new String[] {},
        new String[] {TEST_SUMMARY}));
    Set<String> alerts = result.getServiceAlerts();
    assertEquals(1, alerts.size());
    assertEquals(markup(TEST_SUMMARY), alerts.toArray()[0]);
    assertEquals("name not expected", ROUTE_ID, result.getId());
  }

  @Test
  public void testGetRouteResultServiceAlertWithDescriptionsAndSummaries() {
    RouteResult result = runGetRouteResult(createServiceAlerts(
        new String[] {TEST_DESCRIPTION}, new String[] {TEST_SUMMARY}));
    Set<String> alerts = result.getServiceAlerts();
    assertEquals(1, alerts.size());
    // NOTE!  we now merge and markup SUMMARY + DESCRIPTION, we no longer ignore SUMMARY
    assertEquals(markup(TEST_SUMMARY, TEST_DESCRIPTION), alerts.toArray()[0]);
    assertEquals("name not expected", ROUTE_ID, result.getId());
  }

  private String markup(String description) {
    return "<strong>" + description + "</strong>";
  }
  private String markup(String summary, String description) {
    return "<strong>" + summary + "</strong><br/><br/>" + description;
  }
  // getStopResult tests
  
  @Test
  public void testGetStopResultServiceAlertWithNoDescriptionsOrSummaries() {
    StopResult result = runGetStopResult(createServiceAlerts(new String[] {},
        new String[] {}));
    assertEquals(1, result.getAllRoutesAvailable().size());
    Set<String> alerts = result.getAllRoutesAvailable().get(0).getServiceAlerts();
    assertEquals(1, alerts.size());
    assertEquals("(no description)", alerts.toArray()[0]);
    assertEquals("name not expected", TEST_STOP_ID, result.getId());
  }

  @Test
  public void testGetStopResultServiceAlertWithDescriptionsOnly() {
    // this behaviour changes -- summaries and descriptions are merged together
    StopResult result = runGetStopResult(createServiceAlerts(new String[] {
        TEST_DESCRIPTION, TEST_DESCRIPTION2}, new String[] {TEST_SUMMARY}));
    assertEquals(1, result.getAllRoutesAvailable().size());
    Set<String> alerts = result.getAllRoutesAvailable().get(0).getServiceAlerts();
    assertEquals(3, alerts.size());
    String[] array = alerts.toArray(new String[] {});
    boolean found0 = false, found1 = false, found2 = false;
    for (String s : array) {
      if (TEST_DESCRIPTION.equals(s)) found0 = true;
      if (TEST_DESCRIPTION2.equals(s)) found1 = true;
      if (markup(TEST_SUMMARY).equals(s)) found2 = true;
    }
    assertTrue(found0);
    assertTrue(found1);
    assertTrue(found2);
    assertEquals("name not expected", TEST_STOP_ID, result.getId());
  }

  @Test
  public void testGetStopResultServiceAlertWithSummariesOnly() {
    StopResult result = runGetStopResult(createServiceAlerts(new String[] {},
        new String[] {TEST_SUMMARY}));
    assertEquals(1, result.getAllRoutesAvailable().size());
    Set<String> alerts = result.getAllRoutesAvailable().get(0).getServiceAlerts();
    assertEquals(1, alerts.size());
    assertEquals("<strong>" + TEST_SUMMARY + "</strong>", alerts.toArray()[0]);
    assertEquals("name not expected", TEST_STOP_ID, result.getId());
  }

  @Test
  public void testGetStopResultServiceAlertWithDescriptionsAndSummaries() {
    StopResult result = runGetStopResult(createServiceAlerts(
        new String[] {TEST_DESCRIPTION}, new String[] {TEST_SUMMARY}));
    assertEquals(1, result.getAllRoutesAvailable().size());
    Set<String> alerts = result.getAllRoutesAvailable().get(0).getServiceAlerts();
    assertEquals(1, alerts.size());
    // NOTE!!! this changed to a merged summary + description from just a summary
    assertEquals(markup(TEST_SUMMARY, TEST_DESCRIPTION), alerts.toArray()[0]);
    assertEquals("name not expected", TEST_STOP_ID, result.getId());
  }


  // Support methods
  
  private StopResult runGetStopResult(List<ServiceAlertBean> serviceAlerts) {
    StopsForRouteBean stopsForRouteBean = mock(StopsForRouteBean.class);
    List<StopGroupingBean> stopGroupingBeans = new ArrayList<StopGroupingBean>();
    when(stopsForRouteBean.getStopGroupings()).thenReturn(stopGroupingBeans);
    
    StopGroupingBean stopGroupingBean = mock(StopGroupingBean.class);
    stopGroupingBeans.add(stopGroupingBean);
    
    List<StopGroupBean> stopGroups = new ArrayList<StopGroupBean>();
    StopGroupBean stopGroupBean = mock(StopGroupBean.class);
    stopGroups.add(stopGroupBean );
    when(stopGroupingBean.getStopGroups()).thenReturn(stopGroups );
    
    List<String> stopIds = new ArrayList<String>();
    when(stopGroupBean.getStopIds()).thenReturn(stopIds );
    NameBean nameBean = mock(NameBean.class);
    when(nameBean.getType()).thenReturn("destination");
    when(stopGroupBean.getName()).thenReturn(nameBean);
    List<String> stopGroupBeanStopIds = new ArrayList<String>();
    stopGroupBeanStopIds.add(TEST_STOP_ID);
    when(stopGroupBean.getStopIds()).thenReturn(stopGroupBeanStopIds );
    when(stopGroupBean.getId()).thenReturn(TEST_STOP_ID);
    
    stopIds.add(TEST_STOP_ID);
    
    List<RouteBean> routeBeans = new ArrayList<RouteBean>();
    routeBeans.add(createRouteBean());
    StopBean stopBean = mock(StopBean.class);
    when(stopBean.getId()).thenReturn(TEST_STOP_ID);
    when(stopBean.getRoutes()).thenReturn(routeBeans );

    List<MonitoredStopVisitStructure> monitoredStopVisits = new ArrayList<MonitoredStopVisitStructure>();
    MonitoredStopVisitStructure monitoredStopVisitStructure = mock(MonitoredStopVisitStructure.class);
    monitoredStopVisits.add(monitoredStopVisitStructure);
    
    MonitoredVehicleJourneyStructure monVehJourney = mock(MonitoredVehicleJourneyStructure.class);
    when(monitoredStopVisitStructure.getMonitoredVehicleJourney()).thenReturn(monVehJourney);
    when(monitoredStopVisitStructure.getRecordedAtTime()).thenReturn(new Date(TEST_TIME));
    
    LineRefStructure lineRefStructure = mock(LineRefStructure.class);
    when(monVehJourney.getLineRef()).thenReturn(lineRefStructure );
    when(lineRefStructure.getValue()).thenReturn(ROUTE_ID);
    
    DirectionRefStructure directionRef = mock(DirectionRefStructure.class);
    when(monVehJourney.getDirectionRef()).thenReturn(directionRef );
    when(directionRef.getValue()).thenReturn(TEST_STOP_ID);
    
    NaturalLanguageStringStructure natLangStrStructure = mock(NaturalLanguageStringStructure.class);
    when(natLangStrStructure.getValue()).thenReturn(TEST_DESTINATION_NAME);
    when(monVehJourney.getDestinationName()).thenReturn(natLangStrStructure);
    
    MonitoredCallStructure monCall = mock(MonitoredCallStructure.class);
    ExtensionsStructure extensions = mock(ExtensionsStructure.class);
    SiriExtensionWrapper siriExtensionWrapper = mock(SiriExtensionWrapper.class);
    SiriDistanceExtension distances = mock(SiriDistanceExtension.class);
    when(distances.getPresentableDistance()).thenReturn(TEST_PRESENTABLE_DISTANCE);
    when(siriExtensionWrapper.getDistances()).thenReturn(distances);
    when(extensions.getAny()).thenReturn(siriExtensionWrapper);
    when(monCall.getExtensions()).thenReturn(extensions );
    when(monVehJourney.getMonitoredCall()).thenReturn(monCall );
    
    when(_realtimeService.getMonitoredStopVisitsForStop(eq(TEST_STOP_ID), eq(0), anyLong())).thenReturn(monitoredStopVisits );

    when(_transitDataService.getStopsForRoute(anyString())).thenReturn(
        stopsForRouteBean);
    when(_realtimeService.getServiceAlertsForRouteAndDirection(ROUTE_ID, TEST_STOP_ID)).thenReturn(
        serviceAlerts);
    SearchResultFactoryImpl srf = new SearchResultFactoryImpl(
        _transitDataService, _realtimeService, _configurationService);
    Set<RouteBean> routeFilter = new HashSet<RouteBean>();
    StopResult result = (StopResult) srf.getStopResult(stopBean, routeFilter);
    return result;
  }

  private RouteResult runGetRouteResult(List<ServiceAlertBean> serviceAlerts) {
    StopsForRouteBean stopsForRouteBean = mock(StopsForRouteBean.class);
    when(_transitDataService.getStopsForRoute(anyString())).thenReturn(
        stopsForRouteBean);
    when(_realtimeService.getServiceAlertsForRoute(ROUTE_ID)).thenReturn(
        serviceAlerts);
    SearchResultFactoryImpl srf = new SearchResultFactoryImpl(
        _transitDataService, _realtimeService, _configurationService);
    RouteResult result = (RouteResult) srf.getRouteResult(createRouteBean());
    return result;
  }

  private RouteBean createRouteBean() {
    Builder builder = RouteBean.builder();
    builder.setId(ROUTE_ID);
    RouteBean routeBean = builder.create();
    return routeBean;
  }

  private List<ServiceAlertBean> createServiceAlerts(String[] descriptions,
      String[] summaries) {
    List<ServiceAlertBean> serviceAlerts = new ArrayList<ServiceAlertBean>();
    ServiceAlertBean saBean = new ServiceAlertBean();
    serviceAlerts.add(saBean);
    if (descriptions.length > 0)
      saBean.setDescriptions(createTextList(descriptions));
    if (summaries.length > 0)
      saBean.setSummaries(createTextList(summaries));
    return serviceAlerts;
  }

  private List<NaturalLanguageStringBean> createTextList(String[] texts) {
    List<NaturalLanguageStringBean> textList = new ArrayList<NaturalLanguageStringBean>();
    for (String text : texts) {
      textList.add(new NaturalLanguageStringBean(text, "EN"));
    }
    return textList;
  }

}
