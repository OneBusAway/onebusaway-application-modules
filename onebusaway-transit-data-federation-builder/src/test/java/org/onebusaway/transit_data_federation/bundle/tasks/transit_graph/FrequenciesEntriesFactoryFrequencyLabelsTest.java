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
package org.onebusaway.transit_data_federation.bundle.tasks.transit_graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.addStopTime;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.block;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.lsid;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.route;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stop;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stopTime;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.time;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.trip;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.gtfs.model.Frequency;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data_federation.impl.transit_graph.RouteEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TransitGraphImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;

public class FrequenciesEntriesFactoryFrequencyLabelsTest {

  private FrequencyEntriesFactory _factory;

  private GtfsRelationalDao _dao;

  private TransitGraphImpl _graph;

  private RouteEntryImpl _routeEntry;

  private LocalizedServiceId _lsid;

  private StopEntryImpl _stopA;

  private StopEntryImpl _stopB;

  private StopEntryImpl _stopC;

  private TripEntryImpl _tripEntryA;

  private TripEntryImpl _tripEntryB;

  private TripEntryImpl _tripEntryC;

  private TripEntryImpl _tripEntryD;

  @Before
  public void before() {
    _factory = new FrequencyEntriesFactory();

    _dao = Mockito.mock(GtfsRelationalDao.class);
    _factory.setGtfsDao(_dao);

    _graph = new TransitGraphImpl();

    _routeEntry = route("route");
    _lsid = lsid("serviceId");
    _stopA = stop("stopA");
    _stopB = stop("stopB");
    _stopC = stop("stopC");

    _tripEntryA = trip("tripA").setRoute(_routeEntry).setServiceId(_lsid).setBlock(
        block("blockA")).setDirectionId("0");
    _graph.putTripEntry(_tripEntryA);
    addStopTime(_tripEntryA, stopTime().setStop(_stopA).setTime(time(7, 00)));
    addStopTime(_tripEntryA, stopTime().setStop(_stopB).setTime(time(7, 10)));
    addStopTime(_tripEntryA, stopTime().setStop(_stopC).setTime(time(7, 20)));

    _tripEntryB = trip("tripB").setRoute(_routeEntry).setServiceId(_lsid).setBlock(
        block("blockB")).setDirectionId("1");
    _graph.putTripEntry(_tripEntryB);
    addStopTime(_tripEntryB, stopTime().setStop(_stopA).setTime(time(7, 30)));
    addStopTime(_tripEntryB, stopTime().setStop(_stopB).setTime(time(7, 40)));
    addStopTime(_tripEntryB, stopTime().setStop(_stopC).setTime(time(7, 50)));

    _tripEntryC = trip("tripC").setRoute(_routeEntry).setServiceId(_lsid).setBlock(
        block("blockC")).setDirectionId("0");
    _graph.putTripEntry(_tripEntryC);
    addStopTime(_tripEntryC, stopTime().setStop(_stopA).setTime(time(9, 30)));
    addStopTime(_tripEntryC, stopTime().setStop(_stopB).setTime(time(9, 40)));
    addStopTime(_tripEntryC, stopTime().setStop(_stopC).setTime(time(9, 50)));

    _tripEntryD = trip("tripD").setRoute(_routeEntry).setServiceId(_lsid).setBlock(
        block("blockD")).setDirectionId("1");
    _graph.putTripEntry(_tripEntryD);
    addStopTime(_tripEntryD, stopTime().setStop(_stopA).setTime(time(7, 30)));
    addStopTime(_tripEntryD, stopTime().setStop(_stopC).setTime(time(7, 50)));
  }

  @Test
  public void testFrequencyLabelByRoute() {
    Trip tripA = new Trip();
    tripA.setId(_tripEntryA.getId());

    Frequency frequency = new Frequency();
    frequency.setTrip(tripA);
    frequency.setStartTime(time(7, 00));
    frequency.setEndTime(time(9, 00));
    frequency.setHeadwaySecs(600);
    frequency.setLabelOnly(1);

    Mockito.when(_dao.getAllFrequencies()).thenReturn(Arrays.asList(frequency));

    _graph.initialize();

    _factory.processFrequencies(_graph);

    FrequencyEntry frequencyEntry = _tripEntryA.getFrequencyLabel();
    assertNotNull(frequencyEntry);
    assertEquals(frequency.getStartTime(), frequencyEntry.getStartTime());
    assertEquals(frequency.getEndTime(), frequencyEntry.getEndTime());
    assertEquals(frequency.getHeadwaySecs(), frequencyEntry.getHeadwaySecs());

    assertSame(frequencyEntry, _tripEntryB.getFrequencyLabel());
    assertNull(_tripEntryC.getFrequencyLabel());
    assertSame(frequencyEntry, _tripEntryD.getFrequencyLabel());
  }

  @Test
  public void testFrequencyLabelByDirection() {
    Trip tripA = new Trip();
    tripA.setId(_tripEntryA.getId());

    Frequency frequency = new Frequency();
    frequency.setTrip(tripA);
    frequency.setStartTime(time(7, 00));
    frequency.setEndTime(time(10, 00));
    frequency.setHeadwaySecs(600);
    frequency.setLabelOnly(2);

    Mockito.when(_dao.getAllFrequencies()).thenReturn(Arrays.asList(frequency));

    _graph.initialize();

    _factory.processFrequencies(_graph);

    FrequencyEntry frequencyEntry = _tripEntryA.getFrequencyLabel();
    assertNotNull(frequencyEntry);
    assertEquals(frequency.getStartTime(), frequencyEntry.getStartTime());
    assertEquals(frequency.getEndTime(), frequencyEntry.getEndTime());
    assertEquals(frequency.getHeadwaySecs(), frequencyEntry.getHeadwaySecs());

    assertNull(_tripEntryB.getFrequencyLabel());
    assertSame(frequencyEntry, _tripEntryC.getFrequencyLabel());
    assertNull(_tripEntryD.getFrequencyLabel());
  }
  
  @Test
  public void testFrequencyLabelByStops() {
    Trip tripA = new Trip();
    tripA.setId(_tripEntryA.getId());

    Frequency frequency = new Frequency();
    frequency.setTrip(tripA);
    frequency.setStartTime(time(7, 00));
    frequency.setEndTime(time(10, 00));
    frequency.setHeadwaySecs(600);
    frequency.setLabelOnly(3);

    Mockito.when(_dao.getAllFrequencies()).thenReturn(Arrays.asList(frequency));

    _graph.initialize();

    _factory.processFrequencies(_graph);

    FrequencyEntry frequencyEntry = _tripEntryA.getFrequencyLabel();
    assertNotNull(frequencyEntry);
    assertEquals(frequency.getStartTime(), frequencyEntry.getStartTime());
    assertEquals(frequency.getEndTime(), frequencyEntry.getEndTime());
    assertEquals(frequency.getHeadwaySecs(), frequencyEntry.getHeadwaySecs());

    assertSame(frequencyEntry, _tripEntryB.getFrequencyLabel());
    assertSame(frequencyEntry, _tripEntryC.getFrequencyLabel());
    assertNull(_tripEntryD.getFrequencyLabel());
  }
}
