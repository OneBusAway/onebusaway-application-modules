/**
 * Copyright (C) 2012 Google, Inc.
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
package org.onebusaway.transit_data_federation.impl.reporting;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onebusaway.collections.tuple.T2;
import org.onebusaway.collections.tuple.Tuples;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.problems.EProblemReportStatus;
import org.onebusaway.transit_data.model.problems.ETripProblemGroupBy;
import org.onebusaway.transit_data.model.problems.TripProblemReportQueryBean;
import org.onebusaway.transit_data_federation.services.reporting.UserReportingDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@ContextConfiguration(locations = "classpath:org/onebusaway/transit_data_federation/application-context-test.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class UserReportingDaoImplTest {

  @Autowired
  private UserReportingDao _dao;

  @Autowired
  private SessionFactory _sessionFactory;

  @Before
  public void setup() throws IOException {
  }

  @Test
  @Transactional
  public void test() {
    TripProblemReportRecord r1 = new TripProblemReportRecord();
    r1.setLabel("label-1");
    r1.setStatus(EProblemReportStatus.NEW);
    r1.setTime(1000);
    r1.setTripId(new AgencyAndId("1", "trip-1"));
    _dao.saveOrUpdate(r1);

    TripProblemReportRecord r2 = new TripProblemReportRecord();
    r2.setLabel("label-2");
    r2.setStatus(EProblemReportStatus.NEW);
    r2.setTime(2000);
    r2.setTripId(new AgencyAndId("1", "trip-1"));
    _dao.saveOrUpdate(r2);

    TripProblemReportRecord r3 = new TripProblemReportRecord();
    r3.setLabel("label-2");
    r3.setStatus(EProblemReportStatus.VERIFIED);
    r3.setTime(3000);
    r3.setTripId(new AgencyAndId("2", "trip-1"));
    _dao.saveOrUpdate(r3);

    List<T2<Object, Integer>> summaries = _dao.getTripProblemReportSummaries(
        query(null, null, 0, 0, null, null), ETripProblemGroupBy.TRIP);
    assertEquals(2, summaries.size());
    assertEquals(Tuples.tuple(new AgencyAndId("1", "trip-1"), 2),
        summaries.get(0));
    assertEquals(Tuples.tuple(new AgencyAndId("2", "trip-1"), 1),
        summaries.get(1));

    summaries = _dao.getTripProblemReportSummaries(
        query(null, "1_trip-1", 0, 0, null, null), ETripProblemGroupBy.LABEL);
    assertEquals(2, summaries.size());
    assertEquals(Tuples.tuple("label-1", 1), summaries.get(0));
    assertEquals(Tuples.tuple("label-2", 1), summaries.get(1));

    summaries = _dao.getTripProblemReportSummaries(
        query(null, null, 1500, 2500, null, null), ETripProblemGroupBy.TRIP);
    assertEquals(1, summaries.size());
    assertEquals(Tuples.tuple(new AgencyAndId("1", "trip-1"), 1),
        summaries.get(0));

    summaries = _dao.getTripProblemReportSummaries(
        query(null, null, 0, 0, EProblemReportStatus.NEW, null),
        ETripProblemGroupBy.TRIP);
    assertEquals(1, summaries.size());
    assertEquals(Tuples.tuple(new AgencyAndId("1", "trip-1"), 2),
        summaries.get(0));

    summaries = _dao.getTripProblemReportSummaries(
        query(null, null, 0, 0, null, "label-2"), ETripProblemGroupBy.TRIP);
    assertEquals(2, summaries.size());
    assertEquals(Tuples.tuple(new AgencyAndId("1", "trip-1"), 1),
        summaries.get(0));
    assertEquals(Tuples.tuple(new AgencyAndId("2", "trip-1"), 1),
        summaries.get(1));

    summaries = _dao.getTripProblemReportSummaries(
        query(null, null, 0, 0, null, null), ETripProblemGroupBy.STATUS);
    assertEquals(2, summaries.size());
    assertEquals(Tuples.tuple(EProblemReportStatus.NEW, 2), summaries.get(0));
    assertEquals(Tuples.tuple(EProblemReportStatus.VERIFIED, 1),
        summaries.get(1));

    summaries = _dao.getTripProblemReportSummaries(
        query(null, null, 0, 0, null, null), ETripProblemGroupBy.LABEL);
    assertEquals(2, summaries.size());
    assertEquals(Tuples.tuple("label-1", 1), summaries.get(0));
    assertEquals(Tuples.tuple("label-2", 2), summaries.get(1));

    /**
     * getTripProblemReports(...)
     */
    List<TripProblemReportRecord> records = _dao.getTripProblemReports(query(
        "1", null, 0, 0, null, null));
    assertEquals(2, records.size());

    records = _dao.getTripProblemReports(query("2", null, 0, 0, null, null));
    assertEquals(1, records.size());

    records = _dao.getTripProblemReports(query(null, "1_trip-1", 0, 0, null,
        null));
    assertEquals(2, records.size());

    records = _dao.getTripProblemReports(query(null, null, 1500, 2500, null,
        null));
    assertEquals(1, records.size());

    records = _dao.getTripProblemReports(query(null, null, 1500, 0, null, null));
    assertEquals(2, records.size());

    records = _dao.getTripProblemReports(query(null, null, 0, 0,
        EProblemReportStatus.NEW, null));
    assertEquals(2, records.size());

    records = _dao.getTripProblemReports(query(null, null, 0, 0,
        EProblemReportStatus.VERIFIED, null));
    assertEquals(1, records.size());

    records = _dao.getTripProblemReports(query(null, null, 0, 0,
        EProblemReportStatus.ACCEPTED, null));
    assertEquals(0, records.size());

    records = _dao.getTripProblemReports(query(null, null, 0, 0, null,
        "label-1"));
    assertEquals(1, records.size());

    records = _dao.getTripProblemReports(query(null, null, 0, 0, null,
        "label-2"));
    assertEquals(2, records.size());

    records = _dao.getTripProblemReports(query(null, null, 0, 0, null,
        "label-?"));
    assertEquals(0, records.size());

    records = _dao.getTripProblemReports(query("1", null, 0, 0, null, "label-2"));
    assertEquals(1, records.size());
  }

  private TripProblemReportQueryBean query(String agencyId, String tripId,
      long timeFrom, long timeTo, EProblemReportStatus status, String label) {
    TripProblemReportQueryBean query = new TripProblemReportQueryBean();
    query.setAgencyId(agencyId);
    query.setTripId(tripId);
    query.setTimeFrom(timeFrom);
    query.setTimeTo(timeTo);
    query.setStatus(status);
    query.setLabel(label);
    return query;
  }
}
