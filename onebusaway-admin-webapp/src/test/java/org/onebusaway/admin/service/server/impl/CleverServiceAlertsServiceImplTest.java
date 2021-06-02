package org.onebusaway.admin.service.server.impl;

import org.junit.Test;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectsBean;
import org.onebusaway.transit_data.model.service_alerts.TimeRangeBean;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class CleverServiceAlertsServiceImplTest {

  private static String API_RESULT = "{\n" +
          "        \"bustime-response\": {\n" +
          "                \"dtrs\": [\n" +
          "                        {\n" +
          "                                \"id\": \"888F268C-9D40-4456-9CAF-D3D64E3F3C55\",\n" +
          "                                \"ver\": 3,\n" +
          "                                \"st\": 1,\n" +
          "                                \"desc\": \"AT1 SB SNOW ROUTE\",\n" +
          "                                \"rtdirs\": [\n" +
          "                                        {\n" +
          "                                                \"rt\": \"AT1\",\n" +
          "                                                \"dir\": \"SOUTHBOUND\"\n" +
          "                                        }\n" +
          "                                ],\n" +
          "                                \"startdt\": \"20210209 00:00\",\n" +
          "                                \"enddt\": \"20210211 15:00\"\n" +
          "                        }\n" +
          "                ]\n" +
          "        }\n" +
          "}\n";

  private static String API_RESULT_CANCELED = "{\n" +
          "        \"bustime-response\": {\n" +
          "                \"dtrs\": [\n" +
          "                        {\n" +
          "                                \"id\": \"888F268C-9D40-4456-9CAF-D3D64E3F3C55\",\n" +
          "                                \"ver\": 3,\n" +
          "                                \"st\": 0,\n" +
          "                                \"desc\": \"AT1 SB SNOW ROUTE\",\n" +
          "                                \"rtdirs\": [\n" +
          "                                        {\n" +
          "                                                \"rt\": \"AT1\",\n" +
          "                                                \"dir\": \"SOUTHBOUND\"\n" +
          "                                        }\n" +
          "                                ],\n" +
          "                                \"startdt\": \"20210209 00:00\",\n" +
          "                                \"enddt\": \"20210211 15:00\"\n" +
          "                        }\n" +
          "                ]\n" +
          "        }\n" +
          "}\n";

  @Test
  public void parseAlertsFromStream() {
    CleverServiceAlertsServiceImpl impl = new CleverServiceAlertsServiceImpl();
    List<ServiceAlertBean> serviceAlertBeans = impl.parseAlertsFromStream(getStreamFromString(API_RESULT));
    assertNotNull(serviceAlertBeans);
    assertEquals(1, serviceAlertBeans.size());
    ServiceAlertBean bean = serviceAlertBeans.get(0);
    assertEquals("888F268C-9D40-4456-9CAF-D3D64E3F3C55", bean.getId());
    assertNotNull(bean.getSummaries());
    assertEquals("AT1 SB SNOW ROUTE", bean.getSummaries().get(0).getValue());
    assertNotNull(bean.getAllAffects());
    assertEquals(1, bean.getAllAffects().size());
    SituationAffectsBean situationAffectsBean = bean.getAllAffects().get(0);
    assertNull(situationAffectsBean.getAgencyId());
    assertEquals("71", situationAffectsBean.getAgencyPartRouteId());
    assertEquals("AT1", situationAffectsBean.getRoutePartRouteId());
    assertNotNull(bean.getActiveWindows());
    assertEquals(1, bean.getActiveWindows().size());
    TimeRangeBean timeRangeBean = bean.getActiveWindows().get(0);
    assertEquals((long)impl.parseDate("20210209 00:00"), timeRangeBean.getFrom());
    assertEquals((long)impl.parseDate("20210211 15:00"), timeRangeBean.getTo());
  }

  @Test
  public void parseCancelledAlertsFromStream() {
    CleverServiceAlertsServiceImpl impl = new CleverServiceAlertsServiceImpl();
    List<ServiceAlertBean> serviceAlertBeans = impl.parseAlertsFromStream(getStreamFromString(API_RESULT_CANCELED));
    assertEquals(0, serviceAlertBeans.size());
  }


  @Test
  public void parseRemappedRoute() {
    CleverServiceAlertsServiceImpl impl = new CleverServiceAlertsServiceImpl();
    Map<String, String> map = new HashMap<>();
    map.put("AT1", "AT1x");
    impl.setRouteMapping(map);
    impl.setAgencyId("1");
    impl.setDetourMessage("Route on Detour!");
    List<ServiceAlertBean> serviceAlertBeans = impl.parseAlertsFromStream(getStreamFromString(API_RESULT));
    assertNotNull(serviceAlertBeans);
    assertEquals(1, serviceAlertBeans.size());
    ServiceAlertBean bean = serviceAlertBeans.get(0);
    assertNotNull(bean.getAllAffects());
    assertEquals(1, bean.getAllAffects().size());
    SituationAffectsBean situationAffectsBean = bean.getAllAffects().get(0);

    assertNull(situationAffectsBean.getAgencyId());
    assertEquals("1", situationAffectsBean.getAgencyPartRouteId());
    assertEquals("AT1x", situationAffectsBean.getRoutePartRouteId());

    assertNotNull(bean.getSummaries());
    assertEquals("Route on Detour!", bean.getSummaries().get(0).getValue());


  }

  private InputStream getStreamFromString(String apiResult) {
    return new ByteArrayInputStream(apiResult.getBytes());
  }
}