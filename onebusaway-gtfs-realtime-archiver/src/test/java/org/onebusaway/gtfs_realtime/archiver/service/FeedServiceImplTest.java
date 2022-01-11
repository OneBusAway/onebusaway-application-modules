/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
package org.onebusaway.gtfs_realtime.archiver.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.hibernate.Query;
import org.hibernate.Session;
import org.onebusaway.gtfs_realtime.archiver.listener.GtfsRealtimeEntitySource;
import org.onebusaway.gtfs_realtime.model.AlertModel;
import org.onebusaway.gtfs_realtime.model.EntitySelectorModel;
import org.onebusaway.gtfs_realtime.model.TimeRangeModel;

import com.google.transit.realtime.GtfsRealtime.Alert;
import com.google.transit.realtime.GtfsRealtime.EntitySelector;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedHeader;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TimeRange;
import com.google.transit.realtime.GtfsRealtime.TranslatedString;
import com.google.transit.realtime.GtfsRealtime.TranslatedString.Translation;
import com.google.transit.realtime.GtfsRealtimeConstants;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
    "classpath:service-alerts-data-sources.xml",
    "classpath:org/onebusaway/archiver/application-context-testing.xml"})
@Transactional(transactionManager = "transactionManager")
public class FeedServiceImplTest {
  private GtfsRealtimeEntitySource _entitySource;

  private static final long NOW = System.currentTimeMillis();
  private static final long DAY = 24 * 60 * 60 * 1000;
  private static final String TEST_1 = "Test Alert 1";
  private static final String TEST_2 = "Test Alert 2";
  private static final String TEST_3 = "Test Alert 3";
  private static final String DESC_1 = "Construction delay at Mass Ave and Boylston";
  private static final String DESC_2 = "Car stuck on tracks at Heath St";
  private static final String DESC_3 = "Flooding near Park St";
  private static final Alert.Cause CAUSE_1 = Alert.Cause.CONSTRUCTION;
  private static final Alert.Cause CAUSE_2 = Alert.Cause.ACCIDENT;
  private static final Alert.Cause CAUSE_3 = Alert.Cause.WEATHER;
  private static final Alert.Effect EFFECT_1 = Alert.Effect.SIGNIFICANT_DELAYS;
  private static final Alert.Effect EFFECT_2 = Alert.Effect.DETOUR;
  private static final Alert.Effect EFFECT_3 = Alert.Effect.REDUCED_SERVICE;
  private static final String URL_1 = "http://SomeUrl.org";
  private static final String URL_2 = "http://AnotherUrl.org";
  private static final String URL_3 = "http://ThirdUrl.org";
  private static final long TIME_START_1 = NOW;
  private static final long TIME_START_2 = NOW - DAY;
  private static final long TIME_START_3 = NOW - (DAY * 2);
  private static final long TIME_END_1 = NOW + DAY;
  private static final long TIME_END_2 = NOW + (DAY * 2);
  private static final long TIME_END_3 = NOW + (DAY * 3);
  private static final String AGENCY_1 = "1";
  private static final String AGENCY_2 = "3";
  private static final String AGENCY_3 = "19";
  private static final String ROUTE_1 = null;
  private static final String ROUTE_2 = "1225";
  private static final String ROUTE_3 = null;
  private static final String STOP_1 = null;
  private static final String STOP_2 = null;
  private static final String STOP_3 = "402";

  private static Logger _log = LoggerFactory.getLogger(FeedServiceImpl.class);

  @Autowired
  @Qualifier("feedServiceImpl")
  private FeedService _feedService;

  @Autowired
//  @Qualifier("gtfsRealtimeArchiveSessionFactory")
  private SessionFactory _sessionFactory;

  private Session getSession() {
    return _sessionFactory.getCurrentSession();
  }

  @Before
  public void setup() throws IOException {
    _entitySource = Mockito.mock(GtfsRealtimeEntitySource.class);
  }

  @Test
  @Transactional
  public void testReadAlerts() {
    // Create GTFS Feed with service alerts
    FeedEntity alertEntityA = createAlert("alertA", TEST_1, DESC_1, CAUSE_1,
        EFFECT_1, URL_1, TIME_START_1, TIME_END_1, AGENCY_1, ROUTE_1, STOP_1);
    FeedEntity alertEntityB = createAlert("alertB", TEST_2, DESC_2, CAUSE_2,
        EFFECT_2, URL_2, TIME_START_2, TIME_END_2, AGENCY_2, ROUTE_2, STOP_2);
    FeedEntity alertEntityC = createAlert("alertC", TEST_3, DESC_3, CAUSE_3,
        EFFECT_3, URL_3, TIME_START_3, TIME_END_3, AGENCY_3, ROUTE_3, STOP_3);

    // Create FeedMessage
    FeedMessage.Builder alerts = createFeed();
    alerts.addEntity(alertEntityA);
    alerts.addEntity(alertEntityB);
    alerts.addEntity(alertEntityC);
    FeedMessage alert = alerts.build();

    _feedService.readAlerts(alert, _entitySource);
    Collection<AlertModel> alertsFromDB = null;
    // Wait for 15 seconds to make sure GtfsPersistor has had time to run
    // the AlertThread, which actually writes to the DB.
    try {
      TimeUnit.SECONDS.sleep(15);
    } catch (Exception ignoredEx) {
    }

    // Get data that was persisted to the database
    try {
      Query query = getSession().createQuery("from AlertModel");
      alertsFromDB = query.list();
    } catch (Exception ex) {
      ex.getMessage();
      _log.info("find failed: " + ex.getMessage());
    }
    assertNotNull("database query failed!", alertsFromDB);
    // Check persisted data against the original value.
    _log.info("results size: " + alertsFromDB.size());
    assertEquals(3, alertsFromDB.size());
    for (AlertModel alertFromDB : alertsFromDB) {
      String header = alertFromDB.getHeaderText();
      String desc = alertFromDB.getDescriptionText();
      String cause = alertFromDB.getCause();
      String effect = alertFromDB.getEffect();
      String url = alertFromDB.getUrl();
      long timeStart = 0L;
      long timeEnd = 0L;
      List<TimeRangeModel> timeRanges = alertFromDB.getTimeRanges();
      for (TimeRangeModel tr : timeRanges) {
        timeStart = tr.getStart();
        timeEnd = tr.getEnd();
      }
      String agency = "";
      String route = "";
      String stop = "";
      List<EntitySelectorModel> entitySelectors = alertFromDB.getEntitySelectors();
      for (EntitySelectorModel es : entitySelectors) {
        agency = es.getAgencyId();
        route = es.getRouteId();
        stop = es.getStopId();
      }
      if (header.equals(TEST_1)) {
        assertEquals(TEST_1, header);
        assertEquals(DESC_1, desc);
        assertEquals(CAUSE_1.toString(), cause);
        assertEquals(EFFECT_1.toString(), effect);
        assertEquals(URL_1.toString(), url);
        assertEquals(TIME_START_1, timeStart);
        assertEquals(TIME_END_1, timeEnd);
        assertEquals(AGENCY_1, agency);
        assertEquals(ROUTE_1, route);
        assertEquals(STOP_1, stop);
      } else if (header.equals(TEST_2)) {
        assertEquals(TEST_2, header);
        assertEquals(DESC_2, desc);
        assertEquals(CAUSE_2.toString(), cause);
        assertEquals(EFFECT_2.toString(), effect);
        assertEquals(URL_2.toString(), url);
        assertEquals(TIME_START_2, timeStart);
        assertEquals(TIME_END_2, timeEnd);
        assertEquals(AGENCY_2, agency);
        assertEquals(AGENCY_2 + "_" + ROUTE_2, route); // Verify that agency has
                                                       // been prepended to
                                                       // route.
        assertEquals(STOP_2, stop);
      } else if (header.equals(TEST_3)) {
        assertEquals(TEST_3, header);
        assertEquals(DESC_3, desc);
        assertEquals(CAUSE_3.toString(), cause);
        assertEquals(EFFECT_3.toString(), effect);
        assertEquals(URL_3.toString(), url);
        assertEquals(TIME_START_3, timeStart);
        assertEquals(TIME_END_3, timeEnd);
        assertEquals(AGENCY_3, agency);
        assertEquals(ROUTE_3, route);
        assertEquals(AGENCY_3 + "_" + STOP_3, stop); // Verify that agency has
                                                     // been prepended to stop.
      }
    }
  }

  private FeedEntity createAlert(String alertId, String header, String desc,
      Alert.Cause cause, Alert.Effect effect, String url, long startTime,
      long endTime, String agency, String route, String stop) {
    Alert.Builder alertBldr = Alert.newBuilder();

    // Header
    Translation translation = Translation.newBuilder().setLanguage(
        "en").setText(header).build();
    TranslatedString trnStr = TranslatedString.newBuilder().addTranslation(
        translation).build();
    alertBldr.setHeaderText(trnStr);

    // Description
    translation = Translation.newBuilder().setLanguage("en").setText(
        desc).build();
    trnStr = TranslatedString.newBuilder().addTranslation(translation).build();
    alertBldr.setDescriptionText(trnStr);

    // Cause
    alertBldr.setCause(cause);

    // Effect
    alertBldr.setEffect(effect);

    // URL
    translation = Translation.newBuilder().setLanguage("en").setText(
        url).build();
    trnStr = TranslatedString.newBuilder().addTranslation(translation).build();
    alertBldr.setUrl(trnStr);

    // Build TimeRangeEntity
    TimeRange timeRange = createTimeRange(startTime, endTime);
    alertBldr.addActivePeriod(timeRange);

    // Build EntitySelectorEntity
    EntitySelector entitySelector = createEntitySelector(agency, route, stop);
    alertBldr.addInformedEntity(entitySelector);

    FeedEntity.Builder alertEntity = FeedEntity.newBuilder();
    alertEntity.setId(alertId);
    alertEntity.setAlert(alertBldr.build());
    return alertEntity.build();
  }

  private TimeRange createTimeRange(long startTime, long endTime) {
    TimeRange.Builder timeRange = TimeRange.newBuilder();
    timeRange.setStart(startTime);
    timeRange.setEnd(endTime);
    return timeRange.build();
  }

  private EntitySelector createEntitySelector(String agencyId, String route,
      String stop) {
    EntitySelector.Builder entitySelector = EntitySelector.newBuilder();
    entitySelector.setAgencyId(agencyId);
    if (route != null) {
      entitySelector.setRouteId(route);
    }
    if (stop != null) {
      entitySelector.setStopId(stop);
    }
    return entitySelector.build();
  }

  private FeedMessage.Builder createFeed() {
    FeedMessage.Builder builder = FeedMessage.newBuilder();
    FeedHeader.Builder header = FeedHeader.newBuilder();
    header.setGtfsRealtimeVersion(GtfsRealtimeConstants.VERSION);
    builder.setHeader(header);
    return builder;
  }
}