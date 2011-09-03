/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.transit_data_federation.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.addServiceDates;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.block;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.blockConfiguration;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.date;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.dateAsLong;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.lsids;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.serviceIds;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.time;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.timeZone;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.gtfs.impl.calendar.CalendarServiceImpl;
import org.onebusaway.gtfs.model.calendar.CalendarServiceData;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.model.calendar.ServiceInterval;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.testing.UnitTestingSupport;

public class ExtendedCalendarServiceImplTest {

  private ExtendedCalendarServiceImpl _service;

  private CalendarServiceImpl _calendarService;

  private ServiceInterval interval;

  private TransitGraphDao _transitGraphDao;

  @Before
  public void before() {

    _calendarService = new CalendarServiceImpl();

    CalendarServiceData data = new CalendarServiceData();
    _calendarService.setData(data);

    addServiceDates(data, "sA", new ServiceDate(2010, 9, 10), new ServiceDate(
        2010, 9, 11));
    addServiceDates(data, "sB", new ServiceDate(2010, 9, 11), new ServiceDate(
        2010, 9, 12));
    addServiceDates(data, "sC", new ServiceDate(2010, 9, 12), new ServiceDate(
        2010, 9, 13));
    addServiceDates(data, "sD", new ServiceDate(2010, 9, 13));

    interval = new ServiceInterval(time(9, 00), time(9, 05), time(10, 00),
        time(10, 05));

    _transitGraphDao = Mockito.mock(TransitGraphDao.class);

    _service = new ExtendedCalendarServiceImpl();
    _service.setCalendarService(_calendarService);
    _service.setTransitGraphDao(_transitGraphDao);
  }

  @Test
  public void testGetServiceDatesWithinRange01() {

    ServiceIdActivation serviceIds = serviceIds(lsids("sA"), lsids("sB"));

    Date from = UnitTestingSupport.date("2010-09-10 09:30");
    Date to = UnitTestingSupport.date("2010-09-10 10:30");

    Collection<Date> dates = _service.getServiceDatesWithinRange(serviceIds,
        interval, from, to);

    assertEquals(1, dates.size());
    assertTrue(dates.contains(new ServiceDate(2010, 9, 10).getAsDate(timeZone())));

    from = UnitTestingSupport.date("2010-09-11 09:30");
    to = UnitTestingSupport.date("2010-09-11 10:30");

    dates = _service.getServiceDatesWithinRange(serviceIds, interval, from, to);

    assertEquals(0, dates.size());
  }

  @Test
  public void testGetServiceDatesForServiceIds01() {

    ServiceIdActivation serviceIds = serviceIds(lsids("sA"), lsids("sB"));

    Set<Date> dates = _service.getDatesForServiceIds(serviceIds);

    assertEquals(1, dates.size());
    assertTrue(dates.contains(new ServiceDate(2010, 9, 10).getAsDate(timeZone())));
  }

  @Test
  public void testGetServiceDatesWithinRange02() {

    ServiceIdActivation serviceIds = serviceIds(lsids("sA", "sB"), lsids());

    Date from = UnitTestingSupport.date("2010-09-11 09:30");
    Date to = UnitTestingSupport.date("2010-09-11 10:30");

    Collection<Date> dates = _service.getServiceDatesWithinRange(serviceIds,
        interval, from, to);

    assertEquals(1, dates.size());
    assertTrue(dates.contains(new ServiceDate(2010, 9, 11).getAsDate(timeZone())));

    from = UnitTestingSupport.date("2010-09-10 09:30");
    to = UnitTestingSupport.date("2010-09-10 10:30");

    dates = _service.getServiceDatesWithinRange(serviceIds, interval, from, to);

    assertEquals(0, dates.size());
  }

  @Test
  public void testGetServiceDatesForServiceIds02() {

    ServiceIdActivation serviceIds = serviceIds(lsids("sA", "sB"), lsids());

    Set<Date> dates = _service.getDatesForServiceIds(serviceIds);

    assertEquals(1, dates.size());
    assertTrue(dates.contains(new ServiceDate(2010, 9, 11).getAsDate(timeZone())));
  }

  @Test
  public void testGetServiceDatesWithinRange03() {

    ServiceIdActivation serviceIds = serviceIds(lsids("sC", "sD"), lsids());

    Date from = UnitTestingSupport.date("2010-09-13 09:30");
    Date to = UnitTestingSupport.date("2010-09-13 10:30");

    Collection<Date> dates = _service.getServiceDatesWithinRange(serviceIds,
        interval, from, to);

    assertEquals(1, dates.size());
    assertTrue(dates.contains(new ServiceDate(2010, 9, 13).getAsDate(timeZone())));

    from = UnitTestingSupport.date("2010-09-12 09:30");
    to = UnitTestingSupport.date("2010-09-12 10:30");

    dates = _service.getServiceDatesWithinRange(serviceIds, interval, from, to);

    assertEquals(0, dates.size());
  }

  @Test
  public void testGetServiceDatesForServiceIds03() {

    ServiceIdActivation serviceIds = serviceIds(lsids("sC", "sD"), lsids());

    Set<Date> dates = _service.getDatesForServiceIds(serviceIds);

    assertEquals(1, dates.size());
    assertTrue(dates.contains(new ServiceDate(2010, 9, 13).getAsDate(timeZone())));
  }

  @Test
  public void testGetServiceDatesWithinRange04() {

    ServiceIdActivation serviceIds = serviceIds(lsids("sA", "sC"), lsids());

    Date from = UnitTestingSupport.date("2010-09-10 09:30");
    Date to = UnitTestingSupport.date("2010-09-10 10:30");

    Collection<Date> dates = _service.getServiceDatesWithinRange(serviceIds,
        interval, from, to);

    assertEquals(0, dates.size());

    from = UnitTestingSupport.date("2010-09-12 09:30");
    to = UnitTestingSupport.date("2010-09-12 10:30");

    dates = _service.getServiceDatesWithinRange(serviceIds, interval, from, to);

    assertEquals(0, dates.size());
  }

  @Test
  public void testGetServiceDatesForServiceIds04() {

    ServiceIdActivation serviceIds = serviceIds(lsids("sA", "sC"), lsids());

    Set<Date> dates = _service.getDatesForServiceIds(serviceIds);

    assertEquals(0, dates.size());
  }

  @Test
  public void testGetNextServiceDatesForDepartureInterval() {

    BlockEntry blockA = block("blockA");
    blockConfiguration(blockA, serviceIds(lsids("sA"), lsids()));

    List<BlockEntry> blocks = Arrays.asList(blockA);

    Mockito.when(_transitGraphDao.getAllBlocks()).thenReturn(blocks);

    _service.start();

    ServiceIdActivation serviceIds = serviceIds(lsids("sA"), lsids());
    int inFrom = time(8, 00);
    int inTo = time(20, 00);
    ServiceInterval interval = new ServiceInterval(inFrom, inFrom, inTo, inTo);
    long time = dateAsLong("2010-09-10 09:30");

    List<Date> dates = _service.getNextServiceDatesForDepartureInterval(
        serviceIds, interval, time);

    assertEquals(1, dates.size());
    assertEquals(date("2010-09-10 00:00"), dates.get(0));

    time = dateAsLong("2010-09-10 21:30");
    dates = _service.getNextServiceDatesForDepartureInterval(serviceIds,
        interval, time);

    assertEquals(1, dates.size());
    assertEquals(date("2010-09-11 00:00"), dates.get(0));

    time = dateAsLong("2010-09-11 21:30");
    dates = _service.getNextServiceDatesForDepartureInterval(serviceIds,
        interval, time);

    assertEquals(0, dates.size());
  }

  @Test
  public void testGetPreviousServiceDatesForArrivalInterval() {

    BlockEntry blockA = block("blockA");
    blockConfiguration(blockA, serviceIds(lsids("sA"), lsids()));

    List<BlockEntry> blocks = Arrays.asList(blockA);

    Mockito.when(_transitGraphDao.getAllBlocks()).thenReturn(blocks);

    _service.start();

    ServiceIdActivation serviceIds = serviceIds(lsids("sA"), lsids());
    int inFrom = time(8, 00);
    int inTo = time(20, 00);
    ServiceInterval interval = new ServiceInterval(inFrom, inFrom, inTo, inTo);
    long time = dateAsLong("2010-09-11 21:30");

    List<Date> dates = _service.getPreviousServiceDatesForArrivalInterval(
        serviceIds, interval, time);

    assertEquals(1, dates.size());
    assertEquals(date("2010-09-11 00:00"), dates.get(0));

    time = dateAsLong("2010-09-11 18:00");
    dates = _service.getPreviousServiceDatesForArrivalInterval(serviceIds,
        interval, time);

    assertEquals(1, dates.size());
    assertEquals(date("2010-09-11 00:00"), dates.get(0));

    time = dateAsLong("2010-09-11 07:00");
    dates = _service.getPreviousServiceDatesForArrivalInterval(serviceIds,
        interval, time);

    assertEquals(1, dates.size());
    assertEquals(date("2010-09-10 00:00"), dates.get(0));

    time = dateAsLong("2010-09-10 21:30");
    dates = _service.getPreviousServiceDatesForArrivalInterval(serviceIds,
        interval, time);

    assertEquals(1, dates.size());
    assertEquals(date("2010-09-10 00:00"), dates.get(0));

    time = dateAsLong("2010-09-10 18:00");
    dates = _service.getPreviousServiceDatesForArrivalInterval(serviceIds,
        interval, time);

    assertEquals(1, dates.size());
    assertEquals(date("2010-09-10 00:00"), dates.get(0));

    time = dateAsLong("2010-09-10 07:00");
    dates = _service.getPreviousServiceDatesForArrivalInterval(serviceIds,
        interval, time);

    assertEquals(0, dates.size());
  }
}
