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
import static org.junit.Assert.assertSame;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.addStopTime;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.aid;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.lsid;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stop;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stopTime;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.trip;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data_federation.impl.transit_graph.BlockEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.RouteEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TransitGraphImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
import org.onebusaway.transit_data_federation.testing.UnitTestingSupport;

public class BlockEntriesFactoryTest {

  private BlockEntriesFactory _factory;

  private BlockConfigurationEntriesFactory _blockConfigFactory;

  private GtfsRelationalDao _dao;

  private TransitGraphImpl _graph;

  private Route _route;

  private RouteEntryImpl _routeEntry;

  @Before
  public void before() {
    _factory = new BlockEntriesFactory();

    _blockConfigFactory = Mockito.mock(BlockConfigurationEntriesFactory.class);
    _factory.setBlockConfigurationEntriesFactory(_blockConfigFactory);

    _dao = Mockito.mock(GtfsRelationalDao.class);
    _factory.setGtfsDao(_dao);

    _graph = new TransitGraphImpl();

    _route = new Route();
    _route.setId(aid("route"));
    _routeEntry = UnitTestingSupport.route("route");
    
    Mockito.when(_dao.getAllRoutes()).thenReturn(Arrays.asList(_route));
  }

  @Test
  public void testFixedScheduleBlocks() {

    LocalizedServiceId lsid = lsid("serviceId");
    StopEntryImpl stop = stop("stop");

    Trip tripA = new Trip();
    tripA.setId(aid("tripA"));
    tripA.setRoute(_route);
    tripA.setBlockId("blockA");
    TripEntryImpl tripEntryA = trip("tripA").setRoute(_routeEntry).setServiceId(
        lsid);
    _graph.putTripEntry(tripEntryA);
    addStopTime(tripEntryA, stopTime().setStop(stop));

    Trip tripB = new Trip();
    tripB.setId(aid("tripB"));
    tripB.setRoute(_route);
    tripB.setBlockId("blockA");
    TripEntryImpl tripEntryB = trip("tripB").setRoute(_routeEntry).setServiceId(
        lsid);
    _graph.putTripEntry(tripEntryB);
    addStopTime(tripEntryB, stopTime().setStop(stop));

    Trip tripC = new Trip();
    tripC.setId(aid("tripC"));
    tripC.setRoute(_route);
    tripC.setBlockId("blockB");
    TripEntryImpl tripEntryC = trip("tripC").setRoute(_routeEntry).setServiceId(
        lsid);
    _graph.putTripEntry(tripEntryC);
    addStopTime(tripEntryC, stopTime().setStop(stop));

    Mockito.when(_dao.getTripsForRoute(_route)).thenReturn(
        Arrays.asList(tripA, tripB, tripC));

    _graph.initialize();

    _factory.processBlocks(_graph);

    List<BlockEntryImpl> blocks = _graph.getBlocks();
    assertEquals(2, blocks.size());

    // jre8 changes this ordering so explicity search
    BlockEntryImpl block = find(blocks, "blockB");
    assertEquals(aid("blockB"), block.getId());
    assertSame(block, tripEntryC.getBlock());
    Mockito.verify(_blockConfigFactory).processBlockConfigurations(block,
        Arrays.asList(tripEntryC));

    // jre8 changes this ordering so explicity search
    block = find(blocks, "blockA");
    assertEquals(aid("blockA"), block.getId());
    assertSame(block, tripEntryA.getBlock());
    assertSame(block, tripEntryB.getBlock());
    Mockito.verify(_blockConfigFactory).processBlockConfigurations(block,
        Arrays.asList(tripEntryA, tripEntryB));

    Mockito.verifyNoMoreInteractions(_blockConfigFactory);
  }

  private BlockEntryImpl find(List<BlockEntryImpl> blocks, String searchBlockId) {
    for (BlockEntryImpl i : blocks) {
      if (searchBlockId.equals(i.getId().getId())) {
        return i;
      }
    }
    return null;
  }
}
