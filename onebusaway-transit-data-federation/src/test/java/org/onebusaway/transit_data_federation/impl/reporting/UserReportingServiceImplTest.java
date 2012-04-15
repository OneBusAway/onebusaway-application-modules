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
import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.collections.tuple.Tuples;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.problems.EProblemReportStatus;
import org.onebusaway.transit_data.model.problems.ETripProblemGroupBy;
import org.onebusaway.transit_data.model.problems.TripProblemReportQueryBean;
import org.onebusaway.transit_data.model.problems.TripProblemReportSummaryBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data_federation.services.beans.TripBeanService;
import org.onebusaway.transit_data_federation.services.reporting.UserReportingDao;

public class UserReportingServiceImplTest {

  private UserReportingServiceImpl _service;
  private UserReportingDao _dao;
  private TripBeanService _tripBeanService;

  @Before
  public void before() {
    _service = new UserReportingServiceImpl();

    _dao = Mockito.mock(UserReportingDao.class);
    _service.setUserReportingDao(_dao);

    _tripBeanService = Mockito.mock(TripBeanService.class);
    _service.setTripBeanService(_tripBeanService);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void test() {

    TripProblemReportQueryBean query = new TripProblemReportQueryBean();

    AgencyAndId tripIdA = new AgencyAndId("1", "t-a");
    AgencyAndId tripIdB = new AgencyAndId("1", "t-b");

    TripBean tripA = new TripBean();
    TripBean tripB = new TripBean();

    Mockito.when(
        _dao.getTripProblemReportSummaries(query, ETripProblemGroupBy.TRIP)).thenReturn(
        Arrays.asList(Tuples.tuple((Object) tripIdA, 7),
            Tuples.tuple((Object) tripIdB, 3)));
    Mockito.when(_tripBeanService.getTripForId(tripIdA)).thenReturn(tripA);
    Mockito.when(_tripBeanService.getTripForId(tripIdB)).thenReturn(tripB);

    ListBean<TripProblemReportSummaryBean> summaries = _service.getTripProblemReportSummaries(
        query, ETripProblemGroupBy.TRIP);
    List<TripProblemReportSummaryBean> list = summaries.getList();
    assertEquals(2, list.size());
    TripProblemReportSummaryBean summary = list.get(0);
    assertSame(tripA, summary.getTrip());
    assertEquals(7, summary.getCount());
    summary = list.get(1);
    assertSame(tripB, summary.getTrip());
    assertEquals(3, summary.getCount());

    Mockito.when(
        _dao.getTripProblemReportSummaries(query, ETripProblemGroupBy.STATUS)).thenReturn(
        Arrays.asList(Tuples.tuple((Object) EProblemReportStatus.NEW, 6),
            Tuples.tuple((Object) EProblemReportStatus.DUPLICATE, 4)));

    summaries = _service.getTripProblemReportSummaries(query,
        ETripProblemGroupBy.STATUS);
    list = summaries.getList();
    assertEquals(2, list.size());
    summary = list.get(0);
    assertEquals(EProblemReportStatus.NEW, summary.getStatus());
    assertEquals(6, summary.getCount());
    summary = list.get(1);
    assertEquals(EProblemReportStatus.DUPLICATE, summary.getStatus());
    assertEquals(4, summary.getCount());

    Mockito.when(
        _dao.getTripProblemReportSummaries(query, ETripProblemGroupBy.LABEL)).thenReturn(
        Arrays.asList(Tuples.tuple((Object) "label-a", 9),
            Tuples.tuple((Object) "label-b", 2)));

    summaries = _service.getTripProblemReportSummaries(query,
        ETripProblemGroupBy.LABEL);
    list = summaries.getList();
    assertEquals(2, list.size());
    summary = list.get(0);
    assertEquals("label-a", summary.getLabel());
    assertEquals(9, summary.getCount());
    summary = list.get(1);
    assertEquals("label-b", summary.getLabel());
    assertEquals(2, summary.getCount());
  }
}
