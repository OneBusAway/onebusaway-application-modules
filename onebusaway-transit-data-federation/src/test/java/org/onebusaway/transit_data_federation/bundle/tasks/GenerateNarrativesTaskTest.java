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
package org.onebusaway.transit_data_federation.bundle.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.aid;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.route;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.routeCollection;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stop;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stopTime;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.trip;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data_federation.impl.narrative.NarrativeProviderImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.RouteCollectionEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.RouteEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopTimeEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.model.ShapePointsFactory;
import org.onebusaway.transit_data_federation.model.modifications.Modification;
import org.onebusaway.transit_data_federation.model.modifications.Modifications;
import org.onebusaway.transit_data_federation.model.narrative.AgencyNarrative;
import org.onebusaway.transit_data_federation.model.narrative.RouteCollectionNarrative;
import org.onebusaway.transit_data_federation.model.narrative.StopNarrative;
import org.onebusaway.transit_data_federation.model.narrative.StopTimeNarrative;
import org.onebusaway.transit_data_federation.model.narrative.TripNarrative;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndexService;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteCollectionEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;

public class GenerateNarrativesTaskTest {

  private GenerateNarrativesTask _task;

  private NarrativeProviderImpl _provider;

  private GtfsRelationalDao _gtfsDao;

  private TransitGraphDao _transitGraphDao;

  private ShapePointHelper _shapePointHelper;

  private BlockIndexService _blockIndexService;

  private Modifications _modifications;

  @Before
  public void setup() {
    _task = new GenerateNarrativesTask();
    _provider = new NarrativeProviderImpl();

    _transitGraphDao = Mockito.mock(TransitGraphDao.class);
    _task.setTransitGraphDao(_transitGraphDao);

    _gtfsDao = Mockito.mock(GtfsRelationalDao.class);
    _task.setGtfsDao(_gtfsDao);

    _shapePointHelper = Mockito.mock(ShapePointHelper.class);
    _task.setShapePointHelper(_shapePointHelper);

    _blockIndexService = Mockito.mock(BlockIndexService.class);
    _task.setBlockIndexService(_blockIndexService);

    _modifications = new Modifications();
    _task.setModifications(_modifications);

    _task.setUniqueService(new UniqueServiceImpl());
  }

  @Test
  public void testGenerateAgencyNarratives() {

    Agency agency = new Agency();
    agency.setId("1");
    agency.setLang("en");
    agency.setName("Agency");
    agency.setPhone("555-1234");
    agency.setTimezone("America/New_York");
    agency.setUrl("http://agency.gov/");

    Mockito.when(_gtfsDao.getAllAgencies()).thenReturn(Arrays.asList(agency));

    Modification mod = new Modification();
    mod.setType(AgencyNarrative.class);
    mod.setId("1");
    mod.setProperty("disclaimer");
    mod.setValue("Use at your own risk.");

    _modifications.setModifications(Arrays.asList(mod));

    _task.generateAgencyNarratives(_provider);

    AgencyNarrative narrative = _provider.getNarrativeForAgencyId("1");
    assertEquals(mod.getValue(), narrative.getDisclaimer());
    assertEquals(agency.getLang(), narrative.getLang());
    assertEquals(agency.getName(), narrative.getName());
    assertEquals(agency.getPhone(), narrative.getPhone());
    assertEquals(agency.getTimezone(), narrative.getTimezone());
    assertEquals(agency.getUrl(), narrative.getUrl());
  }

  @Test
  public void testGenerateRouteNarratives() {

    RouteEntryImpl r1 = route("routeA1");
    RouteEntryImpl r2 = route("routeA2");
    RouteCollectionEntryImpl rc = routeCollection("routeA", r1, r2);

    TripEntry t1 = trip("t1");
    TripEntry t2 = trip("t2");
    TripEntry t3 = trip("t3");

    // r2's values should win out over r1's because it has more trips
    r1.setTrips(Arrays.asList(t1));
    r2.setTrips(Arrays.asList(t2, t3));

    Mockito.when(_transitGraphDao.getAllRouteCollections()).thenReturn(
        Arrays.asList((RouteCollectionEntry) rc));

    Route route1 = new Route();
    route1.setId(r1.getId());
    route1.setBikesAllowed(0);
    route1.setColor("#000000");
    route1.setDesc("Route One Desc");
    route1.setLongName("Route One");
    route1.setShortName("One");
    route1.setTextColor("#ff0000");
    route1.setType(3);
    route1.setUrl("http://agency.gov/route-one");

    Route route2 = new Route();
    route2.setId(r2.getId());
    route2.setBikesAllowed(1);
    route2.setColor("#0000ff");
    route2.setDesc("Route Two Desc");
    route2.setLongName("Route Two");
    route2.setShortName("Two");
    route2.setTextColor("#000000");
    route2.setType(3);
    route2.setUrl("http://agency.gov/route-two");

    Mockito.when(_gtfsDao.getRouteForId(r1.getId())).thenReturn(route1);
    Mockito.when(_gtfsDao.getRouteForId(r2.getId())).thenReturn(route2);

    _task.generateRouteNarratives(_provider);

    RouteCollectionNarrative narrative = _provider.getNarrativeForRouteCollectionId(rc.getId());
    assertEquals(route2.getColor(), narrative.getColor());
    assertEquals(route2.getDesc(), narrative.getDescription());
    assertEquals(route2.getLongName(), narrative.getLongName());
    assertEquals(route2.getShortName(), narrative.getShortName());
    assertEquals(route2.getTextColor(), narrative.getTextColor());
    assertEquals(route2.getType(), narrative.getType());
    assertEquals(route2.getUrl(), narrative.getUrl());
  }

  @Test
  public void testGenerateShapePointNarratives() {
    AgencyAndId shapeId = aid("shapeId");
    ShapePoints points = new ShapePoints();
    Mockito.when(_shapePointHelper.getShapePointsForShapeId(shapeId)).thenReturn(
        points);

    Mockito.when(_gtfsDao.getAllShapeIds()).thenReturn(Arrays.asList(shapeId));

    _task.generateShapePointNarratives(_provider);

    Mockito.verify(_shapePointHelper).getShapePointsForShapeId(shapeId);

    assertSame(points, _provider.getShapePointsForId(shapeId));
  }

  @Test
  public void testGenerateStopNarratives() {

    StopEntry stopEntry = stop("stopA", 47.0, -122.0);

    Mockito.when(_transitGraphDao.getAllStops()).thenReturn(
        Arrays.asList(stopEntry));

    Stop stop = new Stop();
    stop.setId(stopEntry.getId());
    stop.setCode("A");
    stop.setDesc("Stop A is the best");
    stop.setLat(stopEntry.getStopLat());
    stop.setLon(stopEntry.getStopLon());
    stop.setName("Stop A");
    stop.setUrl("http://agency.gov/stop-a");

    Mockito.when(_gtfsDao.getAllStops()).thenReturn(Arrays.asList(stop));

    List<BlockStopTimeIndex> indices = Collections.emptyList();
    Mockito.when(_blockIndexService.getStopTimeIndicesForStop(stopEntry)).thenReturn(
        indices);

    _task.generateStopNarratives(_provider);

    StopNarrative narrative = _provider.getNarrativeForStopId(stopEntry.getId());
    assertEquals(stop.getCode(), narrative.getCode());
    assertEquals(stop.getDesc(), narrative.getDescription());
    assertNull(narrative.getDirection());
    assertEquals(stop.getLocationType(), narrative.getLocationType());
    assertEquals(stop.getName(), narrative.getName());
    assertEquals(stop.getUrl(), narrative.getUrl());
  }

  @Test
  public void testGenerateStopNarrativesWithHardCodedDirection() {

    StopEntry stopEntry = stop("stopA", 47.0, -122.0);

    Mockito.when(_transitGraphDao.getAllStops()).thenReturn(
        Arrays.asList(stopEntry));

    Stop stop = new Stop();
    stop.setId(stopEntry.getId());
    stop.setDirection("west");
    Mockito.when(_gtfsDao.getAllStops()).thenReturn(Arrays.asList(stop));

    List<BlockStopTimeIndex> indices = Collections.emptyList();
    Mockito.when(_blockIndexService.getStopTimeIndicesForStop(stopEntry)).thenReturn(
        indices);

    _task.generateStopNarratives(_provider);

    StopNarrative narrative = _provider.getNarrativeForStopId(stopEntry.getId());
    assertEquals("W", narrative.getDirection());
  }

  @Test
  public void testGenerateStopNarrativesWithCalculatedDirection() {

    StopEntryImpl stopEntry = stop("stopA", 47.663146, -122.300928);

    Mockito.when(_transitGraphDao.getAllStops()).thenReturn(
        Arrays.asList((StopEntry) stopEntry));

    Stop stop = new Stop();
    stop.setId(stopEntry.getId());
    Mockito.when(_gtfsDao.getAllStops()).thenReturn(Arrays.asList(stop));

    AgencyAndId shapeId = aid("shapeA");
    ShapePointsFactory factory = new ShapePointsFactory();
    factory.addPoint(47.661225, -122.3009201);
    factory.addPoint(47.664375, -122.3008986);
    ShapePoints shapePoints = factory.create();

    _provider.setShapePointsForId(shapeId, shapePoints);

    TripEntryImpl trip = trip("trip");
    trip.setShapeId(shapeId);

    StopTimeEntryImpl stopTime = stopTime(0, stopEntry, trip, 0, 0.0);
    stopTime.setShapePointIndex(0);

    BlockStopTimeEntry blockStopTime = Mockito.mock(BlockStopTimeEntry.class);
    Mockito.when(blockStopTime.getStopTime()).thenReturn(stopTime);

    BlockStopTimeIndex index = Mockito.mock(BlockStopTimeIndex.class);
    Mockito.when(index.getStopTimes()).thenReturn(Arrays.asList(blockStopTime));

    List<BlockStopTimeIndex> indices = Arrays.asList(index);
    Mockito.when(_blockIndexService.getStopTimeIndicesForStop(stopEntry)).thenReturn(
        indices);

    _task.generateStopNarratives(_provider);

    StopNarrative narrative = _provider.getNarrativeForStopId(stopEntry.getId());
    assertEquals("N", narrative.getDirection());
  }

  @Test
  public void testGenerateStopNarrativesWithConflictingDirections() {

    StopEntryImpl stopEntry = stop("stopA", 47.663146, -122.300928);

    Mockito.when(_transitGraphDao.getAllStops()).thenReturn(
        Arrays.asList((StopEntry) stopEntry));

    Stop stop = new Stop();
    stop.setId(stopEntry.getId());
    Mockito.when(_gtfsDao.getAllStops()).thenReturn(Arrays.asList(stop));

    /**
     * Two shapes heading in opposite directions
     */
    AgencyAndId shapeIdA = aid("shapeA");
    ShapePointsFactory factoryA = new ShapePointsFactory();
    factoryA.addPoint(47.661225, -122.3009201);
    factoryA.addPoint(47.664375, -122.3008986);
    ShapePoints shapePointsA = factoryA.create();
    _provider.setShapePointsForId(shapeIdA, shapePointsA);

    AgencyAndId shapeIdB = aid("shapeB");
    ShapePointsFactory factoryB = new ShapePointsFactory();
    factoryB.addPoint(47.664375, -122.3008986);
    factoryB.addPoint(47.661225, -122.3009201);
    ShapePoints shapePointsB = factoryB.create();
    _provider.setShapePointsForId(shapeIdB, shapePointsB);

    TripEntryImpl tripA = trip("tripA");
    tripA.setShapeId(shapeIdA);

    TripEntryImpl tripB = trip("tripB");
    tripB.setShapeId(shapeIdB);

    StopTimeEntryImpl stopTimeA = stopTime(0, stopEntry, tripA, 0, 0.0);
    stopTimeA.setShapePointIndex(0);

    StopTimeEntryImpl stopTimeB = stopTime(0, stopEntry, tripB, 0, 0.0);
    stopTimeB.setShapePointIndex(0);

    BlockStopTimeEntry blockStopTimeA = Mockito.mock(BlockStopTimeEntry.class);
    Mockito.when(blockStopTimeA.getStopTime()).thenReturn(stopTimeA);

    BlockStopTimeEntry blockStopTimeB = Mockito.mock(BlockStopTimeEntry.class);
    Mockito.when(blockStopTimeB.getStopTime()).thenReturn(stopTimeB);

    BlockStopTimeIndex index = Mockito.mock(BlockStopTimeIndex.class);
    Mockito.when(index.getStopTimes()).thenReturn(
        Arrays.asList(blockStopTimeA, blockStopTimeB));

    List<BlockStopTimeIndex> indices = Arrays.asList(index);
    Mockito.when(_blockIndexService.getStopTimeIndicesForStop(stopEntry)).thenReturn(
        indices);

    _task.generateStopNarratives(_provider);

    StopNarrative narrative = _provider.getNarrativeForStopId(stopEntry.getId());
    assertNull(narrative.getDirection());
  }

  @Test
  public void testGenerateTripNarratives() {

    Trip trip = new Trip();
    trip.setId(aid("trip"));
    trip.setRouteShortName("R1");
    trip.setTripHeadsign("Where are we going?");
    trip.setTripShortName("LOCAL");

    StopTime stopTime = new StopTime();
    stopTime.setRouteShortName("R1X");
    stopTime.setStopHeadsign("Here");

    Mockito.when(_gtfsDao.getAllTrips()).thenReturn(Arrays.asList(trip));
    Mockito.when(_gtfsDao.getStopTimesForTrip(trip)).thenReturn(
        Arrays.asList(stopTime));

    _task.generateTripNarratives(_provider);

    TripNarrative narrative = _provider.getNarrativeForTripId(trip.getId());
    assertEquals(trip.getRouteShortName(), narrative.getRouteShortName());
    assertEquals(trip.getTripHeadsign(), narrative.getTripHeadsign());
    assertEquals(trip.getTripShortName(), narrative.getTripShortName());

    StopEntryImpl stopEntry = stop("stop", 47.0, -122.0);
    TripEntryImpl tripEntry = trip("trip");
    StopTimeEntryImpl stopTimeEntry = stopTime(0, stopEntry, tripEntry, 0, 0.0);

    StopTimeNarrative stopTimeNarrative = _provider.getNarrativeForStopTimeEntry(stopTimeEntry);
    assertEquals(stopTime.getRouteShortName(),
        stopTimeNarrative.getRouteShortName());
    assertEquals(stopTime.getStopHeadsign(),
        stopTimeNarrative.getStopHeadsign());
  }
}
