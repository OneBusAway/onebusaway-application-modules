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
package org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.alerts.impl.ServiceAlertLibrary;
import org.onebusaway.alerts.service.ServiceAlerts;
import org.onebusaway.alerts.service.ServiceAlerts.*;
import com.google.transit.realtime.GtfsRealtime;
import com.google.transit.realtime.GtfsRealtime.Alert;
import com.google.transit.realtime.GtfsRealtime.EntitySelector;
import com.google.transit.realtime.GtfsRealtime.TranslatedString;
import com.google.transit.realtime.GtfsRealtime.TranslatedString.Translation;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;

public class GtfsRealtimeAlertLibraryTest {

  private GtfsRealtimeAlertLibrary _library;
  private GtfsRealtimeEntitySource _entitySource;

  @Before
  public void before() {
    _library = new GtfsRealtimeAlertLibrary();
    _entitySource = Mockito.mock(GtfsRealtimeEntitySource.class);
    _library.setEntitySource(_entitySource);
  }

  @Test
  public void testGetAlertAsServiceAlert() {

    AgencyAndId alertId = new AgencyAndId("1", "A1");
    Alert.Builder alert = Alert.newBuilder();
    GtfsRealtime.TimeRange.Builder timeRange = GtfsRealtime.TimeRange.newBuilder();
    timeRange.setStart(1L);
    timeRange.setStart(2L);
    alert.addActivePeriod(timeRange);

    EntitySelector.Builder entitySelector = EntitySelector.newBuilder();
    entitySelector.setAgencyId("agencyId");
    entitySelector.setRouteId("routeId");
    entitySelector.setStopId("stopId");
    TripDescriptor.Builder tripDescriptor = TripDescriptor.newBuilder();
    tripDescriptor.setTripId("tripId");
    entitySelector.setTrip(tripDescriptor);
    alert.addInformedEntity(entitySelector);

    alert.setCause(Alert.Cause.ACCIDENT);

    alert.setEffect(Alert.Effect.DETOUR);

    TranslatedString.Builder headerTexts = TranslatedString.newBuilder();
    Translation.Builder headerText = Translation.newBuilder();
    headerText.setLanguage("en");
    headerText.setText("Summary");
    headerTexts.addTranslation(headerText);
    alert.setHeaderText(headerTexts);

    TranslatedString.Builder descriptionTexts = TranslatedString.newBuilder();
    Translation.Builder descriptionText = Translation.newBuilder();
    descriptionText.setLanguage("fr");
    descriptionText.setText("Description");
    descriptionTexts.addTranslation(descriptionText);
    alert.setDescriptionText(descriptionTexts);

    TranslatedString.Builder urls = TranslatedString.newBuilder();
    Translation.Builder url = Translation.newBuilder();
    url.setLanguage("es");
    url.setText("http://something/");
    urls.addTranslation(url);
    alert.setUrl(urls);

    Mockito.when(_entitySource.getRouteId("routeId")).thenReturn(
        ServiceAlertLibrary.id("1", "routeId"));
    Mockito.when(_entitySource.getStopId("stopId")).thenReturn(
        ServiceAlertLibrary.id("2", "stopId"));
    Mockito.when(_entitySource.getTripId("tripId")).thenReturn(
        ServiceAlertLibrary.id("3", "tripId"));

    ServiceAlert.Builder serviceAlert = _library.getAlertAsServiceAlert(
        alertId, alert.build());

    Id id = serviceAlert.getId();
    assertEquals("1", id.getAgencyId());
    assertEquals("A1", id.getId());

    assertEquals(1, serviceAlert.getAffectsCount());
    Affects affects = serviceAlert.getAffects(0);
    assertEquals("agencyId", affects.getAgencyId());
    assertEquals("1", affects.getRouteId().getAgencyId());
    assertEquals("routeId", affects.getRouteId().getId());
    assertEquals("2", affects.getStopId().getAgencyId());
    assertEquals("stopId", affects.getStopId().getId());
    assertEquals("3", affects.getTripId().getAgencyId());
    assertEquals("tripId", affects.getTripId().getId());

    assertEquals(ServiceAlert.Cause.ACCIDENT, serviceAlert.getCause());

    assertEquals(1, serviceAlert.getConsequenceCount());
    Consequence consequence = serviceAlert.getConsequence(0);
    assertEquals(Consequence.Effect.DETOUR, consequence.getEffect());

    ServiceAlerts.TranslatedString summaries = serviceAlert.getSummary();
    assertEquals(1, summaries.getTranslationCount());
    ServiceAlerts.TranslatedString.Translation summary = summaries.getTranslation(0);
    assertEquals("en", summary.getLanguage());
    assertEquals("Summary", summary.getText());

    ServiceAlerts.TranslatedString descriptions = serviceAlert.getDescription();
    assertEquals(1, descriptions.getTranslationCount());
    ServiceAlerts.TranslatedString.Translation description = descriptions.getTranslation(0);
    assertEquals("fr", description.getLanguage());
    assertEquals("Description", description.getText());

    ServiceAlerts.TranslatedString alertUrls = serviceAlert.getUrl();
    assertEquals(1, alertUrls.getTranslationCount());
    ServiceAlerts.TranslatedString.Translation alertUrl = alertUrls.getTranslation(0);
    assertEquals("es", alertUrl.getLanguage());
    assertEquals("http://something/", alertUrl.getText());
  }
}
