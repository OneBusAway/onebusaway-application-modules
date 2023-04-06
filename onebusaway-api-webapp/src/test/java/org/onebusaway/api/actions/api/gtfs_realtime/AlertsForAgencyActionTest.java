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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.api.model.ResponseBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.service_alerts.NaturalLanguageStringBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectsBean;
import org.onebusaway.transit_data.model.service_alerts.TimeRangeBean;
import org.onebusaway.transit_data.services.TransitDataService;

import com.google.transit.realtime.GtfsRealtime.Alert;
import com.google.transit.realtime.GtfsRealtime.EntitySelector;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TimeRange;
import com.google.transit.realtime.GtfsRealtime.TranslatedString;
import com.google.transit.realtime.GtfsRealtime.TranslatedString.Translation;

public class AlertsForAgencyActionTest {

  private AlertsForAgencyAction _action;

  private TransitDataService _service;

  @Before
  public void before() {
    _action = new AlertsForAgencyAction();

    _service = Mockito.mock(TransitDataService.class);
    _action.setTransitDataService(_service);
  }

  @Test
  public void test() {
    long now = System.currentTimeMillis();

    List<ServiceAlertBean> alerts = new ArrayList<ServiceAlertBean>();

    {
      ServiceAlertBean alert = new ServiceAlertBean();
      alerts.add(alert);
      TimeRangeBean range = new TimeRangeBean(1234 * 1000, 5678 * 1000);
      alert.setActiveWindows(Arrays.asList(range));
      SituationAffectsBean affects = new SituationAffectsBean();
      affects.setAgencyId("1");
      affects.setRouteId("1_r0");
      affects.setStopId("1_s0");
      affects.setTripId("1_t0");
      SituationAffectsBean alsoAffects = new SituationAffectsBean();
      alsoAffects.setAgencyId("2");
      alert.setAllAffects(Arrays.asList(affects, alsoAffects));
      alert.setSummaries(Arrays.asList(new NaturalLanguageStringBean("Name",
          "en"), new NaturalLanguageStringBean("Nombre", "es")));
      alert.setDescriptions(Arrays.asList(new NaturalLanguageStringBean(
          "Description", "en"), new NaturalLanguageStringBean("Descripci