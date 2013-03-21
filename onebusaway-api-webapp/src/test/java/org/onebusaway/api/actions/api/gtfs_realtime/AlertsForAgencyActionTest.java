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
          "Description", "en"), new NaturalLanguageStringBean("Descripción",
          "es")));
    }
    {
      ServiceAlertBean alert = new ServiceAlertBean();
      alerts.add(alert);
      TimeRangeBean range = new TimeRangeBean(5678 * 1000, 1234 * 1000);
      alert.setActiveWindows(Arrays.asList(range));
      SituationAffectsBean affects = new SituationAffectsBean();
      affects.setAgencyId("2");
      affects.setRouteId("1_r1");
      affects.setStopId("1_s1");
      affects.setTripId("1_t1");
      alert.setAllAffects(Arrays.asList(affects));
      alert.setSummaries(Arrays.asList(new NaturalLanguageStringBean("Name",
          "en")));
      alert.setDescriptions(Arrays.asList(new NaturalLanguageStringBean(
          "Description", "en")));
    }
    ListBean<ServiceAlertBean> bean = new ListBean<ServiceAlertBean>();
    bean.setList(alerts);
    Mockito.when(_service.getAllServiceAlertsForAgencyId("1")).thenReturn(bean);

    _action.setId("1");
    _action.setTime(new Date(now));

    _action.show();

    ResponseBean model = _action.getModel();
    FeedMessage feed = (FeedMessage) model.getData();
    assertEquals(now / 1000, feed.getHeader().getTimestamp());
    assertEquals(2, feed.getEntityCount());

    {
      FeedEntity entity = feed.getEntity(0);
      assertEquals("1", entity.getId());
      Alert alert = entity.getAlert();
      assertEquals(1, alert.getActivePeriodCount());
      TimeRange range = alert.getActivePeriod(0);
      assertEquals(1234, range.getStart());
      assertEquals(5678, range.getEnd());
      assertEquals(2, alert.getInformedEntityCount());
      {
        EntitySelector affects = alert.getInformedEntity(0);
        assertEquals("1", affects.getAgencyId());
        assertEquals("r0", affects.getRouteId());
        assertEquals("t0", affects.getTrip().getTripId());
        assertEquals("s0", affects.getStopId());
      }
      {
        EntitySelector affects = alert.getInformedEntity(1);
        assertEquals("2", affects.getAgencyId());
      }
      TranslatedString header = alert.getHeaderText();
      assertEquals(2, header.getTranslationCount());
      {
        Translation translation = header.getTranslation(0);
        assertEquals("Name", translation.getText());
        assertEquals("en", translation.getLanguage());
      }
      {
        Translation translation = header.getTranslation(1);
        assertEquals("Nombre", translation.getText());
        assertEquals("es", translation.getLanguage());
      }
      TranslatedString description = alert.getDescriptionText();
      assertEquals(2, description.getTranslationCount());
      {
        Translation translation = description.getTranslation(0);
        assertEquals("Description", translation.getText());
        assertEquals("en", translation.getLanguage());
      }
      {
        Translation translation = description.getTranslation(1);
        assertEquals("Descripción", translation.getText());
        assertEquals("es", translation.getLanguage());
      }
    }
    {
      FeedEntity entity = feed.getEntity(1);
      assertEquals("2", entity.getId());
      Alert alert = entity.getAlert();
      assertEquals(1, alert.getActivePeriodCount());
      TimeRange range = alert.getActivePeriod(0);
      assertEquals(5678, range.getStart());
      assertEquals(1234, range.getEnd());
      assertEquals(1, alert.getInformedEntityCount());
      {
        EntitySelector affects = alert.getInformedEntity(0);
        assertEquals("2", affects.getAgencyId());
        assertEquals("r1", affects.getRouteId());
        assertEquals("t1", affects.getTrip().getTripId());
        assertEquals("s1", affects.getStopId());
      }
    }
  }
}
