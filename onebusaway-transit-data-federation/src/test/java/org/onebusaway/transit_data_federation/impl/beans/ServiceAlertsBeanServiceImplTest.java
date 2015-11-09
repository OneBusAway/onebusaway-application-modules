/**
 * Copyright (C) 2011 Google, Inc.
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
/*
package org.onebusaway.transit_data_federation.impl.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.onebusaway.transit_data.model.service_alerts.EEffect;
import org.onebusaway.transit_data.model.service_alerts.ESeverity;
import org.onebusaway.transit_data.model.service_alerts.NaturalLanguageStringBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectsBean;
import org.onebusaway.transit_data.model.service_alerts.SituationConsequenceBean;
import org.onebusaway.transit_data.model.service_alerts.TimeRangeBean;
import org.onebusaway.transit_data_federation.impl.service_alerts.ServiceAlertLibrary;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlerts.Affects;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlerts.Consequence;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlerts.Consequence.Effect;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlerts.ServiceAlert;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlerts.ServiceAlert.Cause;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlerts.ServiceAlert.Severity;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlerts.TimeRange;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlerts.TranslatedString;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlerts.TranslatedString.Translation;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlertsService;

public class ServiceAlertsBeanServiceImplTest {

  private ServiceAlertsBeanServiceImpl _service;
  private ServiceAlertsService _serviceAlertService;

  @Before
  public void setup() {
    _service = new ServiceAlertsBeanServiceImpl();

    _serviceAlertService = Mockito.mock(ServiceAlertsService.class);
    _service.setServiceAlertsService(_serviceAlertService);
  }

  @Test
  public void testCreateServiceAlert() {

    */
/**
     * Construct the bean we'll use in the call to the service
     *//*

    ServiceAlertBean bean = new ServiceAlertBean();
    bean.setActiveWindows(Arrays.asList(new TimeRangeBean(30, 40)));
    SituationAffectsBean affectsBean = new SituationAffectsBean();
    affectsBean.setAgencyId("1");
    affectsBean.setApplicationId("key");
    affectsBean.setDirectionId("0");
    affectsBean.setRouteId("1_route");
    affectsBean.setTripId("1_trip");
    affectsBean.setStopId("1_stop");
    bean.setAllAffects(Arrays.asList(affectsBean));
    SituationConsequenceBean consequenceBean = new SituationConsequenceBean();
    consequenceBean.setEffect(EEffect.DETOUR);
    consequenceBean.setDetourPath("path");
    consequenceBean.setDetourStopIds(Arrays.asList("1_stop"));
    bean.setConsequences(Arrays.asList(consequenceBean));
    bean.setCreationTime(5);
    bean.setDescriptions(Arrays.asList(new NaturalLanguageStringBean(
        "description", "en")));
    bean.setId("1_ignore_this");
    bean.setPublicationWindows(Arrays.asList(new TimeRangeBean(20, 40)));
    bean.setReason("ACCIDENT");
    bean.setSeverity(ESeverity.VERY_SEVERE);
    bean.setSummaries(Arrays.asList(new NaturalLanguageStringBean("summary",
        "en")));
    bean.setUrls(Arrays.asList(new NaturalLanguageStringBean(
        "http://somewhere", "en")));

    */
/**
     * Construct the ServiceAlert.Builder that we'll return from the
     * ServiceAlertService mock when called by the bean service
     *//*

    ServiceAlert.Builder builder = ServiceAlert.newBuilder();
    TimeRange.Builder trBuilder = TimeRange.newBuilder();
    trBuilder.setStart(30);
    trBuilder.setEnd(40);
    builder.addActiveWindow(trBuilder);
    Affects.Builder affectsBuilder = Affects.newBuilder();
    affectsBuilder.setAgencyId("B");
    affectsBuilder.setApplicationId("keyB");
    affectsBuilder.setDirectionId("1");
    affectsBuilder.setRouteId(ServiceAlertLibrary.id("1", "routeB"));
    affectsBuilder.setTripId(ServiceAlertLibrary.id("1", "tripB"));
    affectsBuilder.setStopId(ServiceAlertLibrary.id("1", "stopB"));
    builder.addAffects(affectsBuilder);
    Consequence.Builder consequenceBuilder = Consequence.newBuilder();
    consequenceBuilder.setEffect(Effect.MODIFIED_SERVICE);
    consequenceBuilder.setDetourPath("pathB");
    consequenceBuilder.addDetourStopIds(ServiceAlertLibrary.id("1", "stopB"));
    builder.addConsequence(consequenceBuilder);
    builder.setCreationTime(1000);
    builder.setCause(Cause.CONSTRUCTION);
    TranslatedString.Builder tsBuilder = TranslatedString.newBuilder();
    Translation.Builder tBuilder = Translation.newBuilder();
    tBuilder.setLanguage("fr");
    tBuilder.setText("descriptionB");
    tsBuilder.addTranslation(tBuilder);
    builder.setDescription(tsBuilder);
    builder.setId(ServiceAlertLibrary.id("1", "updated_id"));
    builder.setModifiedTime(System.currentTimeMillis());
    trBuilder = TimeRange.newBuilder();
    trBuilder.setStart(20);
    trBuilder.setEnd(40);
    builder.addPublicationWindow(trBuilder);
    builder.setSeverity(Severity.NO_IMPACT);
    tsBuilder = TranslatedString.newBuilder();
    tBuilder = Translation.newBuilder();
    tBuilder.setLanguage("fr");
    tBuilder.setText("summaryB");
    tsBuilder.addTranslation(tBuilder);
    builder.setSummary(tsBuilder);
    tsBuilder = TranslatedString.newBuilder();
    tBuilder = Translation.newBuilder();
    tBuilder.setLanguage("fr");
    tBuilder.setText("http://somewhere/else/");
    tsBuilder.addTranslation(tBuilder);
    builder.setUrl(tsBuilder);

    ArgumentCaptor<ServiceAlert.Builder> captor = ArgumentCaptor.forClass(ServiceAlert.Builder.class);
    Mockito.when(
        _serviceAlertService.createOrUpdateServiceAlert(captor.capture(),
            Mockito.eq("1"))).thenReturn(builder.build());

    ServiceAlertBean updated = _service.createServiceAlert("1", bean);

    Mockito.verify(_serviceAlertService).createOrUpdateServiceAlert(
        Mockito.any(ServiceAlert.Builder.class), Mockito.eq("1"));

    assertNotSame(updated, bean);

    */
/**
     * Verify that the conversion of the ServiceAlertBean into a
     * ServiceAlert.Builder looks right
     *//*

    builder = captor.getValue();
    assertEquals(1, builder.getActiveWindowCount());
    TimeRange range = builder.getActiveWindow(0);
    assertEquals(30L, range.getStart());
    assertEquals(40L, range.getEnd());
    assertEquals(1, builder.getAffectsCount());
    Affects affects = builder.getAffects(0);
    assertEquals("1", affects.getAgencyId());
    assertEquals("key", affects.getApplicationId());
    assertEquals("0", affects.getDirectionId());
    assertEquals("route", affects.getRouteId().getId());
    assertEquals("trip", affects.getTripId().getId());
    assertEquals("stop", affects.getStopId().getId());
    assertEquals(Cause.ACCIDENT, builder.getCause());
    assertEquals(1, builder.getConsequenceCount());
    Consequence consequence = builder.getConsequence(0);
    assertEquals(Effect.DETOUR, consequence.getEffect());
    assertEquals("path", consequence.getDetourPath());
    assertEquals(1, consequence.getDetourStopIdsCount());
    assertEquals("stop", consequence.getDetourStopIds(0).getId());
    assertEquals(5, builder.getCreationTime());
    assertTrue(builder.hasDescription());
    TranslatedString descs = builder.getDescription();
    assertEquals(1, descs.getTranslationCount());
    Translation desc = descs.getTranslation(0);
    assertEquals("en", desc.getLanguage());
    assertEquals("description", desc.getText());
    assertEquals("ignore_this", builder.getId().getId());
    assertEquals(1, builder.getPublicationWindowCount());
    range = builder.getPublicationWindow(0);
    assertEquals(20L, range.getStart());
    assertEquals(40L, range.getEnd());
    assertEquals(Severity.VERY_SEVERE, builder.getSeverity());
    TranslatedString summaries = builder.getSummary();
    assertEquals(1, summaries.getTranslationCount());
    Translation summary = summaries.getTranslation(0);
    assertEquals("en", summary.getLanguage());
    assertEquals("summary", summary.getText());
    TranslatedString urls = builder.getUrl();
    assertEquals(1, urls.getTranslationCount());
    Translation url = urls.getTranslation(0);
    assertEquals("en", url.getLanguage());
    assertEquals("http://somewhere", url.getText());

    */
/**
     * Verify that the conversion from the ServiceAlert to ServiceAlertBean
     * looks good
     *//*

    List<TimeRangeBean> windows = updated.getActiveWindows();
    assertEquals(1, windows.size());
    TimeRangeBean window = windows.get(0);
    assertEquals(30L, window.getFrom());
    assertEquals(40L, window.getTo());
    assertEquals(1, updated.getAllAffects().size());
    affectsBean = updated.getAllAffects().get(0);
    assertEquals("B", affectsBean.getAgencyId());
    assertEquals("keyB", affectsBean.getApplicationId());
    assertEquals("1", affectsBean.getDirectionId());
    assertEquals("1_routeB", affectsBean.getRouteId());
    assertEquals("1_stopB", affectsBean.getStopId());
    assertEquals("1_tripB", affectsBean.getTripId());
    assertEquals(1, updated.getConsequences().size());
    consequenceBean = updated.getConsequences().get(0);
    assertEquals(EEffect.MODIFIED_SERVICE, consequenceBean.getEffect());
    assertEquals("pathB", consequenceBean.getDetourPath());
    assertEquals(1, consequenceBean.getDetourStopIds().size());
    assertEquals("1_stopB", consequenceBean.getDetourStopIds().get(0));
    assertEquals(1000, updated.getCreationTime());
    assertEquals(1, updated.getDescriptions().size());
    NaturalLanguageStringBean nls = updated.getDescriptions().get(0);
    assertEquals("fr", nls.getLang());
    assertEquals("descriptionB", nls.getValue());
    assertEquals("1_updated_id", updated.getId());
    assertEquals(1, updated.getPublicationWindows().size());
    window = updated.getPublicationWindows().get(0);
    assertEquals(20L, window.getFrom());
    assertEquals(40L, window.getTo());
    assertEquals("CONSTRUCTION", updated.getReason());
    assertEquals(ESeverity.NO_IMPACT, updated.getSeverity());
    assertEquals(1, updated.getSummaries().size());
    nls = updated.getSummaries().get(0);
    assertEquals("fr", nls.getLang());
    assertEquals("summaryB", nls.getValue());
    assertEquals(1, updated.getUrls().size());
    nls = updated.getUrls().get(0);
    assertEquals("fr", nls.getLang());
    assertEquals("http://somewhere/else/", nls.getValue());
  }
}
*/
