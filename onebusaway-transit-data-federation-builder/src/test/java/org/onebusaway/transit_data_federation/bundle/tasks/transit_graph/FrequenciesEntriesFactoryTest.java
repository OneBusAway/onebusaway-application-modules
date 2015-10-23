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
import static org.junit.Assert.fail;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.addStopTime;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.block;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.blockConfiguration;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.lsid;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.route;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.serviceIds;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stop;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stopTime;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.time;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.trip;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.gtfs.model.Frequency;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data_federation.impl.transit_graph.BlockEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.RouteEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TransitGraphImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;

public class FrequenciesEntriesFactoryTest {

  private FrequencyEntriesFactory _factory;

  private GtfsRelationalDao _dao;

  private TransitGraphImpl _graph;

  private RouteEntryImpl _routeEntry;

  private LocalizedServiceId _lsid;

  private StopEntryImpl _stopA;

  private StopEntryImpl _stopB;

  private StopEntryImpl _stopC;

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
  }

  @Test
  public void testSingleTripWithFrequencies() {

    BlockEntryImpl block = block("block");
    TripEntryImpl tripEntryA = trip("trip").setRoute(_routeEntry).setServiceId(
        _lsid).setBlock(block);
    _graph.putTripEntry(tripEntryA);
    addStopTime(tripEntryA, stopTime().setStop(_stopA).setTime(time(7, 00)));

    BlockConfigurationEntry blockConfig = blockConfiguration(block,
        serviceIds("serviceId"), tripEntryA);

    Trip trip = new Trip();
    trip.setId(tripEntryA.getId());

    Frequency freqA = new Frequency();
    freqA.setTrip(trip);
    freqA.setStartTime(time(8, 00));
    freqA.setEndTime(time(10, 00));
    freqA.setHeadwaySecs(10 * 60);
    freqA.setExactTimes(0);

    Frequency freqB = new Frequency();
    freqB.setTrip(trip);
    freqB.setStartTime(time(10, 00));
    freqB.setEndTime(time(12, 00));
    freqB.setHeadwaySecs(10 * 60);
    freqB.setExactTimes(0);

    Mockito.when(_dao.getAllFrequencies()).thenReturn(
        Arrays.asList(freqA, freqB));

    _graph.initialize();

    _factory.processFrequencies(_graph);

    blockConfig = block.getConfigurations().get(0);

    List<FrequencyEntry> frequencies = blockConfig.getFrequencies();
    assertEquals(2, frequencies.size());
    FrequencyEntry frequency = frequencies.get(0);
    assertEquals(freqA.getStartTime(), frequency.getStartTime());
    assertEquals(freqA.getEndTime(), frequency.getEndTime());
    assertEquals(freqA.getHeadwaySecs(), frequency.getHeadwaySecs());
    assertEquals(freqA.getExactTimes(), frequency.getExactTimes());
  }

  @Test
  public void testTripsWithMismatchedFrequencies() {

    BlockEntryImpl block = block("block");
    TripEntryImpl tripEntryA = trip("trip").setRoute(_routeEntry).setServiceId(
        _lsid).setBlock(block);
    _graph.putTripEntry(tripEntryA);
    addStopTime(tripEntryA, stopTime().setStop(_stopA).setTime(time(7, 00)));

    Trip trip = new Trip();
    trip.setId(tripEntryA.getId());

    Frequency freqA = new Frequency();
    freqA.setTrip(trip);
    freqA.setStartTime(time(8, 00));
    freqA.setEndTime(time(10, 00));
    freqA.setHeadwaySecs(10 * 60);
    freqA.setExactTimes(0);

    Frequency freqB = new Frequency();
    freqB.setTrip(trip);
    freqB.setStartTime(time(10, 00));
    freqB.setEndTime(time(12, 00));
    freqB.setHeadwaySecs(10 * 60);

    /**
     * We don't allow a mix of trips with exactTimes=0 and exactTimes=1
     */
    freqB.setExactTimes(1);

    Mockito.when(_dao.getAllFrequencies()).thenReturn(
        Arrays.asList(freqA, freqB));

    _graph.initialize();

    /****
     * Actual Test
     ****/

    try {
      _factory.processFrequencies(_graph);
      fail();
    } catch (IllegalStateException ex) {

    }
  }
}
