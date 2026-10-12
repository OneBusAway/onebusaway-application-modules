/**
 * Copyright (C) 2026 Open Transit Software Foundation
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
package org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.onebusaway.alerts.impl.ServiceAlertRecord;
import org.onebusaway.alerts.service.ServiceAlertsService;
import org.onebusaway.gtfs.model.AgencyAndId;

import com.google.transit.realtime.GtfsRealtime.Alert;
import com.google.transit.realtime.GtfsRealtime.EntitySelector;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedHeader;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TranslatedString;
import com.google.transit.realtime.GtfsRealtime.TranslatedString.Translation;

/**
 * Alerts feeds are polled every refresh interval and usually return the same
 * alerts. Each save is a database round trip, so unchanged alerts must not be
 * saved again.
 */
public class GtfsRealtimeSourceAlertsTest {

  private GtfsRealtimeSource _source;
  private ServiceAlertsService _alertsService;

  @Before
  public void setup() throws Exception {
    _alertsService = mock(ServiceAlertsService.class);
    when(_alertsService.getAllServiceAlerts()).thenReturn(Collections.<ServiceAlertRecord>emptyList());
    // the service holds every saved alert unless a test says otherwise
    when(_alertsService.getServiceAlertForId(any(AgencyAndId.class))).thenReturn(new ServiceAlertRecord());

    _source = new GtfsRealtimeSource();
    _source.setAgencyIds(Collections.singletonList("1"));
    _source.setFeedId("feed");
    _source.setAlertsUrl(new URL("http://localhost/alerts.pb"));
    _source.setServiceAlertService(_alertsService);
    _source.setAlertLibrary(new GtfsRealtimeAlertLibrary());
  }

  @Test
  public void unchangedAlertsAreNotSavedAgain() throws Exception {
    _source.handleAlerts(feed("Detour on route 10"));
    Thread.sleep(5); // conversion stamps the current time; make sure it moves
    _source.handleAlerts(feed("Detour on route 10"));

    assertEquals(1, savedAlertCount());
  }

  @Test
  public void changedAlertsAreSavedAgain() throws Exception {
    _source.handleAlerts(feed("Detour on route 10"));
    Thread.sleep(5);
    _source.handleAlerts(feed("Detour on route 10 has ended"));

    assertEquals(2, savedAlertCount());
  }

  @Test
  public void unchangedAlertsTheServiceLostAreSavedAgain() throws Exception {
    _source.handleAlerts(feed("Detour on route 10"));
    // e.g. pruned when a poll omitted it, or deleted from the admin console
    when(_alertsService.getServiceAlertForId(any(AgencyAndId.class))).thenReturn(null);
    _source.handleAlerts(feed("Detour on route 10"));

    assertEquals(2, savedAlertCount());
  }

  private int savedAlertCount() {
    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<ServiceAlertRecord>> saved = ArgumentCaptor.forClass((Class) List.class);
    verify(_alertsService, atLeastOnce()).createOrUpdateServiceAlerts(anyString(), saved.capture());
    List<ServiceAlertRecord> all = new ArrayList<>();
    for (List<ServiceAlertRecord> batch : saved.getAllValues()) all.addAll(batch);
    return all.size();
  }

  private static FeedMessage feed(String header) {
    Alert alert = Alert.newBuilder()
        .addInformedEntity(EntitySelector.newBuilder().setAgencyId("1"))
        .setHeaderText(TranslatedString.newBuilder()
            .addTranslation(Translation.newBuilder().setText(header).setLanguage("en")))
        .build();
    return FeedMessage.newBuilder()
        .setHeader(FeedHeader.newBuilder().setGtfsRealtimeVersion("2.0").setTimestamp(1700000000L))
        .addEntity(FeedEntity.newBuilder().setId("A1").setAlert(alert))
        .build();
  }
}
