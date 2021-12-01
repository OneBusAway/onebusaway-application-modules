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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.onebusaway.presentation.impl.service_alerts.ServiceAlertsTestSupport;
import org.onebusaway.presentation.services.realtime.RealtimeService;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.service_alerts.SituationQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.siri.SiriJsonSerializer;
import org.onebusaway.transit_data_federation.siri.SiriXmlSerializer;
import org.onebusaway.util.impl.analytics.GoogleAnalyticsServiceImpl;
import org.onebusaway.util.services.configuration.ConfigurationService;

import com.brsanthu.googleanalytics.GoogleAnalyticsRequest;
import com.brsanthu.googleanalytics.GoogleAnalyticsResponse;

import uk.org.siri.siri.LocationStructure;
import uk.org.siri.siri.SituationRefStructure;
import uk.org.siri.siri.SituationSimpleRefStructure;
import uk.org.siri.siri.VehicleActivityStructure;
import uk.org.siri.siri.VehicleActivityStructure.MonitoredVehicleJourney;

@RunWith(MockitoJUnitRunner.class)
public class VehicleMonitoringActionTest extends VehicleMonitoringAction {

  private static final long serialVersionUID = 1L;

  @Mock
  private RealtimeService realtimeService;
  
  @Mock
  private TransitDataService transitDataService;
  
  @Mock
  private ConfigurationService configurationService;
  
  @InjectMocks
  private VehicleMonitoringAction action;

  @Mock
  HttpServletRequest request;
  
  @Mock
  HttpServletResponse servletResponse;
  
  @Mock
  GoogleAnalyticsServiceImpl gaService;
  
  @Test
  public void testExecuteByRoute() throws Exception {
    
    when(request.getParameter(eq("LineRef"))).thenReturn("40_100479");
    when(request.getParameter(eq("OperatorRef"))).thenReturn("1");

    List<VehicleActivityStructure> vehicleActivities = new ArrayList<VehicleActivityStructure>();
    when(realtimeService.getVehicleActivityForRoute(eq("40_100479"), any(), eq(0), anyLong(), eq(false))).thenReturn(vehicleActivities);
    
    VehicleActivityStructure vehicleActivity = new VehicleActivityStructure();
    vehicleActivities.add(vehicleActivity);
    
    MonitoredVehicleJourney mvJourney = new MonitoredVehicleJourney();
    vehicleActivity.setMonitoredVehicleJourney(mvJourney );
    
    LocationStructure locationStructure = new LocationStructure();
    mvJourney.setVehicleLocation(locationStructure );
    
    locationStructure.setLatitude(BigDecimal.valueOf(88.0));
    locationStructure.setLongitude(BigDecimal.valueOf(89.0));
    
    ServiceAlertBean serviceAlertBean = ServiceAlertsTestSupport.createServiceAlertBean("1_1");
    lenient().when(transitDataService.getServiceAlertForId(anyString())).thenReturn(serviceAlertBean );
    
    RouteBean routeBean = RouteBean.builder().create();
    when(transitDataService.getRouteForId(anyString())).thenReturn(routeBean);
    
    lenient().when(configurationService.getConfigurationValueAsString(eq("display.googleAnalyticsSiteId"), anyString())).thenReturn("foo");
    
    List<SituationRefStructure> sitRef = mvJourney.getSituationRef();
    SituationRefStructure sitRefStructure = new SituationRefStructure();
    sitRef.add(sitRefStructure );
    SituationSimpleRefStructure sitSimpleRef = new SituationSimpleRefStructure();
    sitRefStructure.setSituationSimpleRef(sitSimpleRef );
    sitSimpleRef.setValue("situation ref");

    SiriXmlSerializer serializer = new SiriXmlSerializer();
    when(realtimeService.getSiriXmlSerializer()).thenReturn(serializer );
    
    //doNothing().when(gaService).post(new PageViewHit());
    lenient().when(gaService.post(new GoogleAnalyticsRequest())).thenReturn(new GoogleAnalyticsResponse());
    
    action.setServletRequest(request);
    action.setServletResponse(servletResponse);
    action.index();
    String monitoring = action.getSiri();
    assertTrue("Result XML does not match expected", monitoring.matches("(?s).*<ServiceDelivery><ResponseTimestamp>.+</ResponseTimestamp><VehicleMonitoringDelivery><ResponseTimestamp>.+</ResponseTimestamp><ValidUntil>.+</ValidUntil><VehicleActivity><MonitoredVehicleJourney><SituationRef><SituationSimpleRef>situation ref</SituationSimpleRef></SituationRef><VehicleLocation><Longitude>89.0</Longitude><Latitude>88.0</Latitude></VehicleLocation></MonitoredVehicleJourney></VehicleActivity></VehicleMonitoringDelivery><SituationExchangeDelivery><Situations><PtSituationElement><SituationNumber>1_1</SituationNumber><Summary xml:lang=\"EN\">summary</Summary><Description xml:lang=\"EN\">description</Description><Affects><VehicleJourneys><AffectedVehicleJourney><LineRef>1_100277</LineRef><DirectionRef>0</DirectionRef></AffectedVehicleJourney><AffectedVehicleJourney><LineRef>1_100277</LineRef><DirectionRef>1</DirectionRef></AffectedVehicleJourney><AffectedVehicleJourney><LineRef>1_100194</LineRef><DirectionRef>0</DirectionRef></AffectedVehicleJourney><AffectedVehicleJourney><LineRef>1_100194</LineRef><DirectionRef>1</DirectionRef></AffectedVehicleJourney></VehicleJourneys></Affects></PtSituationElement></Situations></SituationExchangeDelivery></ServiceDelivery></Siri>.*"));
  }

  @Test
  public void testExecuteByRouteJson() throws Exception {

    when(request.getParameter(eq("LineRef"))).thenReturn("40_100479");
    when(request.getParameter(eq("OperatorRef"))).thenReturn("1");

    List<VehicleActivityStructure> vehicleActivities = new ArrayList<VehicleActivityStructure>();
    when(realtimeService.getVehicleActivityForRoute(eq("40_100479"), any(), eq(0), anyLong(), eq(false))).thenReturn(vehicleActivities);

    VehicleActivityStructure vehicleActivity = new VehicleActivityStructure();
    vehicleActivities.add(vehicleActivity);

    MonitoredVehicleJourney mvJourney = new MonitoredVehicleJourney();
    vehicleActivity.setMonitoredVehicleJourney(mvJourney );

    LocationStructure locationStructure = new LocationStructure();
    mvJourney.setVehicleLocation(locationStructure );

    locationStructure.setLatitude(BigDecimal.valueOf(88.0));
    locationStructure.setLongitude(BigDecimal.valueOf(89.0));

    ServiceAlertBean serviceAlertBean = ServiceAlertsTestSupport.createServiceAlertBean("1_1");
    lenient().when(transitDataService.getServiceAlertForId(anyString())).thenReturn(serviceAlertBean );

    RouteBean routeBean = RouteBean.builder().create();
    when(transitDataService.getRouteForId(anyString())).thenReturn(routeBean);

    lenient().when(configurationService.getConfigurationValueAsString(eq("display.googleAnalyticsSiteId"), anyString())).thenReturn("foo");

    List<SituationRefStructure> sitRef = mvJourney.getSituationRef();
    SituationRefStructure sitRefStructure = new SituationRefStructure();
    sitRef.add(sitRefStructure );
    SituationSimpleRefStructure sitSimpleRef = new SituationSimpleRefStructure();
    sitRefStructure.setSituationSimpleRef(sitSimpleRef );
    sitSimpleRef.setValue("situation ref");

    SiriJsonSerializer jsonSerializer = new SiriJsonSerializer();
    when(realtimeService.getSiriJsonSerializer()).thenReturn(jsonSerializer);

    lenient().when(gaService.post(new GoogleAnalyticsRequest())).thenReturn(new GoogleAnalyticsResponse());
    action.setType("json");
    action.setServletRequest(request);
    action.setServletResponse(servletResponse);
    action.index();
    String monitoring = action.getSiri();
    String example = "{\"Siri\":{\"ServiceDelivery\":{\"ResponseTimestamp\":\"2021-10-30T09:10:49.019-07:00\",\"VehicleMonitoringDelivery\":[{\"VehicleActivity\":[{\"MonitoredVehicleJourney\":{\"SituationRef\":[{\"SituationSimpleRef\":\"situation ref\"}],\"VehicleLocation\":{\"Longitude\":89.0,\"Latitude\":88.0}}}],\"ResponseTimestamp\":\"2021-10-30T09:10:49.019-07:00\",\"ValidUntil\":\"2021-10-30T09:11:49.019-07:00\"}],\"SituationExchangeDelivery\":[{\"Situations\":{\"PtSituationElement\":[{\"Summary\":\"summary\",\"Description\":\"description\",\"Affects\":{\"VehicleJourneys\":{\"AffectedVehicleJourney\":[{\"LineRef\":\"1_100277\",\"DirectionRef\":\"0\"},{\"LineRef\":\"1_100277\",\"DirectionRef\":\"1\"},{\"LineRef\":\"1_100194\",\"DirectionRef\":\"0\"},{\"LineRef\":\"1_100194\",\"DirectionRef\":\"1\"}]}},\"SituationNumber\":\"1_1\"}]}}]}}}";
    String matchPattern = "{\"Siri\":{\"ServiceDelivery\":{\"ResponseTimestamp\":\"...-..-.T..:..:......-0.:00\",\"VehicleMonitoringDelivery\":[{\"VehicleActivity\":[{\"MonitoredVehicleJourney\":{\"SituationRef\":[{\"SituationSimpleRef\":\"situation ref\"}],\"VehicleLocation\":{\"Longitude\":89.0,\"Latitude\":88.0}}}],\"ResponseTimestamp\":\"....-..-..T..:..:......-0.:00\",\"ValidUntil\":\"...-..-..T..:..:......-0.:00\"}],\"SituationExchangeDelivery\":[{\"Situations\":{\"PtSituationElement\":[{\"Summary\":\"summary\",\"Description\":\"description\",\"Affects\":{\"VehicleJourneys\":{\"AffectedVehicleJourney\":[{\"LineRef\":\"1_100277\",\"DirectionRef\":\"0\"},{\"LineRef\":\"1_100277\",\"DirectionRef\":\"1\"},{\"LineRef\":\"1_100194\",\"DirectionRef\":\"0\"},{\"LineRef\":\"1_100194\",\"DirectionRef\":\"1\"}]}},\"SituationNumber\":\"1_1\"}]}}]}}}";

    String[] expectedFragments = {
            "ServiceDelivery", "ResponseTimestamp", "VehicleMonitoringDelivery", "VehicleActivity",
            "MonitoredVehicleJourney", "SituationRef", "situation ref",
            "VehicleLocation", "\"Longitude\":89.0,\"Latitude\":88.0}",
            "SituationExchangeDelivery", "\"Summary\":\"summary\"",
            "\"Description\":\"description\"", "Affects", "VehicleJourneys", "AffectedVehicleJourney",
            "\"LineRef\":\"1_100277\",\"DirectionRef\":\"0\"", "\"LineRef\":\"1_100277\",\"DirectionRef\":\"1\"",
            "\"LineRef\":\"1_100194\",\"DirectionRef\":\"0\"", "\"LineRef\":\"1_100194\",\"DirectionRef\":\"1\"",
            "\"SituationNumber\":\"1_1\""
    };
    System.out.println("expected=|\n" + monitoring + "\n|");
    for (String s : expectedFragments) {
      boolean expectedMatch = monitoring.contains(s);
      if (!expectedMatch) {
        System.out.println("expected=|" + s + "|");
        System.out.println("actual=|" + monitoring + "|");
      }
      assertTrue("Result JSON does not match expected", expectedMatch);
    }
  }
  @Test
  public void testExecuteByRouteNoActivity() throws Exception {
    
    when(request.getParameter(eq("LineRef"))).thenReturn("40_100479");
    when(request.getParameter(eq("OperatorRef"))).thenReturn("1");
    
    List<VehicleActivityStructure> vehicleActivities = new ArrayList<VehicleActivityStructure>();
    lenient().when(realtimeService.getVehicleActivityForRoute(eq("40_100479"), anyString(), eq(0), anyLong(), eq(false))).thenReturn(vehicleActivities);
    
    ServiceAlertBean serviceAlertBean = ServiceAlertsTestSupport.createServiceAlertBean("1_1");
    lenient().when(transitDataService.getServiceAlertForId(anyString())).thenReturn(serviceAlertBean );
    
    RouteBean routeBean = RouteBean.builder().create();
    when(transitDataService.getRouteForId(anyString())).thenReturn(routeBean);
    
    ListBean<ServiceAlertBean> serviceAlertListBean = new ListBean<ServiceAlertBean>();
    List<ServiceAlertBean> list = new ArrayList<ServiceAlertBean>();
    list.add(serviceAlertBean);
    serviceAlertListBean.setList(list );
    when(transitDataService.getServiceAlerts(any(SituationQueryBean.class))).thenReturn(serviceAlertListBean );
    
    SiriXmlSerializer serializer = new SiriXmlSerializer();
    when(realtimeService.getSiriXmlSerializer()).thenReturn(serializer );

    action.setServletRequest(request);
    action.setServletResponse(servletResponse);
    action.index();
    String monitoring = action.getSiri();
    String regex = "(?s).*<SituationExchangeDelivery><Situations><PtSituationElement><SituationNumber>1_1</SituationNumber><Summary xml:lang=\"EN\">summary</Summary><Description xml:lang=\"EN\">description</Description><Affects><VehicleJourneys><AffectedVehicleJourney><LineRef>1_100277</LineRef><DirectionRef>0</DirectionRef></AffectedVehicleJourney><AffectedVehicleJourney><LineRef>1_100277</LineRef><DirectionRef>1</DirectionRef></AffectedVehicleJourney><AffectedVehicleJourney><LineRef>1_100194</LineRef><DirectionRef>0</DirectionRef></AffectedVehicleJourney><AffectedVehicleJourney><LineRef>1_100194</LineRef><DirectionRef>1</DirectionRef></AffectedVehicleJourney></VehicleJourneys></Affects></PtSituationElement><PtSituationElement><SituationNumber>1_1</SituationNumber><Summary xml:lang=\"EN\">summary</Summary><Description xml:lang=\"EN\">description</Description><Affects><VehicleJourneys><AffectedVehicleJourney><LineRef>1_100277</LineRef><DirectionRef>0</DirectionRef></AffectedVehicleJourney><AffectedVehicleJourney><LineRef>1_100277</LineRef><DirectionRef>1</DirectionRef></AffectedVehicleJourney><AffectedVehicleJourney><LineRef>1_100194</LineRef><DirectionRef>0</DirectionRef></AffectedVehicleJourney><AffectedVehicleJourney><LineRef>1_100194</LineRef><DirectionRef>1</DirectionRef></AffectedVehicleJourney></VehicleJourneys></Affects></PtSituationElement></Situations></SituationExchangeDelivery></ServiceDelivery></Siri>.*";
    boolean success = monitoring.matches(regex);
    if (!success) {
      System.out.println("found=" + monitoring);
      System.out.println("expected=" + regex);
    }
    assertTrue("Result XML does not match expected", success);
  }
}
