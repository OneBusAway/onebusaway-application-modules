/*
 * Copyright 2008 Brian Ferris
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.washington.cs.rse.transit.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import edu.washington.cs.rse.geospatial.IGeoPoint;
import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;
import edu.washington.cs.rse.transit.BaseTest;
import edu.washington.cs.rse.transit.ClearCacheTestHandler;
import edu.washington.cs.rse.transit.common.model.ChangeDate;
import edu.washington.cs.rse.transit.common.model.Route;
import edu.washington.cs.rse.transit.common.model.ServicePattern;
import edu.washington.cs.rse.transit.common.model.ServicePatternKey;
import edu.washington.cs.rse.transit.common.model.StopLocation;
import edu.washington.cs.rse.transit.common.model.TransLink;

import com.carbonfive.testutils.spring.dbunit.DataSet;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;

import org.hibernate.Hibernate;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestExecutionListeners;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@DataSet
@TestExecutionListeners( {ClearCacheTestHandler.class})
public class MetroKCDAOTest extends BaseTest {

  private static GeometryFactory _geomFactory = new GeometryFactory();

  @Autowired
  private MetroKCDAO _dao;

  @BeforeClass
  public static void beforeClass() throws Exception {
    System.out.println("beforeClass");
  }

  @AfterClass
  public static void afterClass() throws Exception {
    System.out.println("afterClass");
  }

  public void testGetElementsById() {

    fail("Not yet implemented");
  }

  /*****************************************************************************
   * Point Methods
   ****************************************************************************/

  public void testGetLatLonAsPoint() {

    Point p1 = _dao.getLatLonAsPoint(47.67007091909418, -122.29031039884515);

    assertEquals(1281462.90, p1.getX(), 0.01);
    assertEquals(247819.26, p1.getY(), 0.01);

    Point p2 = _dao.getLatLonAsPoint(47.66224268868735, -122.31321638275935);
    assertEquals(1275765.36, p2.getX(), 0.01);
    assertEquals(245072.00, p2.getY(), 0.01);
  }

  public void testGetLatLonsAsPoints() {

    List<CoordinatePoint> latlons = new ArrayList<CoordinatePoint>();
    latlons.add(new CoordinatePoint(47.67007091909418, -122.29031039884515));
    latlons.add(new CoordinatePoint(47.66224268868735, -122.31321638275935));
    List<IGeoPoint> points = _dao.getLatLonsAsPoints(latlons);
    assertEquals(2, points.size());

    assertEquals(1281462.90, points.get(0).getX(), 0.01);
    assertEquals(247819.26, points.get(0).getY(), 0.01);

    assertEquals(1275765.36, points.get(1).getX(), 0.01);
    assertEquals(245072.00, points.get(1).getY(), 0.01);
  }

  public void testGetPointAsGeoPoint() {

    Coordinate c = new Coordinate(1275765.36, 245072.00);
    Point point = _geomFactory.createPoint(c);

    IGeoPoint p = _dao.getPointAsGeoPoint(point);
    assertEquals(1275765.36, p.getX(), 0.01);
    assertEquals(245072.00, p.getY(), 0.01);
  }

  public void testGetLocationAsGeoPoint() {
    IGeoPoint p = _dao.getLocationAsGeoPoint(1275765.36, 245072.00);
    assertEquals(1275765.36, p.getX(), 0.01);
    assertEquals(245072.00, p.getY(), 0.01);
  }

  public void testGetPointAsLatLong() {
    Coordinate c = new Coordinate(1275765.36, 245072.00);
    Point point = _geomFactory.createPoint(c);
    CoordinatePoint cp = _dao.getPointAsLatLong(point);
    assertEquals(cp.getLat(), 47.66224268868735);
    assertEquals(cp.getLon(), -122.31321638275935);
  }

  public void testGetPointsAsLatLongs() {

    Coordinate c1 = new Coordinate(1281462.90, 247819.26);
    Coordinate c2 = new Coordinate(1275765.36, 245072.00);

    List<Point> points = new ArrayList<Point>();
    points.add(_geomFactory.createPoint(c1));
    points.add(_geomFactory.createPoint(c2));

    List<CoordinatePoint> cps = _dao.getPointsAsLatLongs(points, points.size());

    assertEquals(2, cps.size());

    CoordinatePoint cp1 = cps.get(1);
    assertEquals(cp1.getLat(), 47.67007091909418);
    assertEquals(cp1.getLon(), -122.29031039884515);

    CoordinatePoint cp2 = cps.get(1);
    assertEquals(cp2.getLat(), 47.66224268868735);
    assertEquals(cp2.getLon(), -122.31321638275935);
  }

  /*****************************************************************************
   * Generic Entity Methods
   ****************************************************************************/

  public void testSave() {
    fail("Not yet implemented");
  }

  public void testSaveAllEntities() {
    fail("Not yet implemented");
  }

  public void testUpdate() {
    fail("Not yet implemented");
  }

  public void testSaveOrUpdate() {
    fail("Not yet implemented");
  }

  public void testSaveOrUpdateAllEntities() {
    fail("Not yet implemented");
  }

  public void testGetEntity() {
    fail("Not yet implemented");
  }

  public void testLoadEntity() {
    fail("Not yet implemented");
  }

  /*****************************************************************************
   * 
   ****************************************************************************/

  public void testGetChangeDateById() {
    ChangeDate cd = _dao.getChangeDateById(109);
    assertNotNull(cd);
    assertEquals(109, cd.getId());
    assertEquals("CURRENT", cd.getCurrentNextCode());
  }

  public void testGetChangeDates() {
    List<ChangeDate> cds = _dao.getChangeDates();
    assertEquals(1, cds.size());
    assertEquals(109, cds.get(0).getId());
  }

  public void testGetCurrentServiceRevision() {
    ChangeDate cd = _dao.getCurrentServiceRevision();
    assertNotNull(cd);
    assertEquals(109, cd.getId());
    assertEquals("CURRENT", cd.getCurrentNextCode());
  }

  /*****************************************************************************
   * 
   ****************************************************************************/

  @Test
  public void testGetAllStopLocations() {
    List<StopLocation> allStopLocations = _dao.getAllStopLocations();
    assertEquals(4, allStopLocations.size());
    for (StopLocation stop : allStopLocations) {
      assertTrue(Hibernate.isInitialized(stop.getMainStreetName()));
      assertTrue(Hibernate.isInitialized(stop.getCrossStreetName()));
      assertFalse(Hibernate.isInitialized(stop.getTransLink()));
    }
  }

  @Test
  public void testGetStopLocationById() {

    StopLocation stopA = _dao.getStopLocationById(10030);
    assertNotNull(stopA);
    assertEquals(10030, stopA.getId());
    assertEquals(1281401.14, stopA.getLocation().getX(), 0.01);
    assertEquals(247251.73, stopA.getLocation().getY(), 0.01);
    assertTrue(Hibernate.isInitialized(stopA.getMainStreetName()));
    assertTrue(Hibernate.isInitialized(stopA.getCrossStreetName()));
    assertEquals(5636, stopA.getMainStreetName().getId());
    assertEquals(1688, stopA.getCrossStreetName().getId());
    assertEquals(56561, stopA.getTransLink().getId());
    assertFalse(Hibernate.isInitialized(stopA.getTransLink()));

    StopLocation stopB = _dao.getStopLocationById(25150);
    assertNotNull(stopB);
    assertEquals(25150, stopB.getId());
    assertEquals(1281479.7830732, stopB.getLocation().getX(), 0.01);
    assertEquals(247159.94741407, stopB.getLocation().getY(), 0.01);
    assertTrue(Hibernate.isInitialized(stopB.getMainStreetName()));
    assertTrue(Hibernate.isInitialized(stopB.getCrossStreetName()));
    assertEquals(1688, stopB.getMainStreetName().getId());
    assertEquals(5636, stopB.getCrossStreetName().getId());
    assertEquals(38637, stopB.getTransLink().getId());
    assertFalse(Hibernate.isInitialized(stopB.getTransLink()));
  }

  @Test
  public void testGetAllStopLocationIds() {
    List<Integer> ids = _dao.getAllStopLocationIds();
    assertEquals(4, ids.size());
    Set<Integer> s = new HashSet<Integer>(ids);
    assertTrue(s.contains(25840));
    assertTrue(s.contains(25150));
    assertTrue(s.contains(10500));
    assertTrue(s.contains(10030));
  }

  @Test
  public void testGetStopLocationsByLocation() {
    Coordinate a = new Coordinate(1281470, 247100);
    Coordinate b = new Coordinate(1281490, 247500);
    MultiPoint mp = _geomFactory.createMultiPoint(new Coordinate[] {a, b});
    Geometry envelope = mp.getEnvelope();
    List<StopLocation> stops = _dao.getStopLocationsByLocation(envelope);
    assertEquals(2, stops.size());
    Map<Integer, StopLocation> stopsById = MetroKCDAO.getElementsById(stops);
    assertTrue(stopsById.containsKey(25150));
    assertTrue(stopsById.containsKey(25840));
    for (StopLocation stop : stops) {
      assertTrue(Hibernate.isInitialized(stop.getMainStreetName()));
      assertTrue(Hibernate.isInitialized(stop.getCrossStreetName()));
      assertFalse(Hibernate.isInitialized(stop.getTransLink()));
    }
  }

  public void testGetStopLocationsByLocationNoLimit() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetStopLocationsByServicePatternServicePattern() {
    ServicePattern pattern = _dao.getActiveServicePatternById(11030002);
    List<StopLocation> stops = _dao.getStopLocationsByServicePattern(pattern);
    assertEquals(1, stops.size());
    StopLocation stop = stops.get(0);
    assertEquals(10500, stop.getId());
    assertTrue(Hibernate.isInitialized(stop.getMainStreetName()));
    assertTrue(Hibernate.isInitialized(stop.getCrossStreetName()));
    assertFalse(Hibernate.isInitialized(stop.getTransLink()));
  }

  @Test
  public void testGetStopLocationsByServicePatternDoNotIncludeTransLink() {

    ServicePattern pattern = _dao.getActiveServicePatternById(11030002);
    List<StopLocation> stops = _dao.getStopLocationsByServicePattern(pattern,
        false);
    assertEquals(1, stops.size());
    StopLocation stop = stops.get(0);
    assertEquals(10500, stop.getId());
    assertTrue(Hibernate.isInitialized(stop.getMainStreetName()));
    assertTrue(Hibernate.isInitialized(stop.getCrossStreetName()));
    TransLink link = stop.getTransLink();
    assertFalse(Hibernate.isInitialized(link));
  }

  @Test
  public void testGetStopLocationsByServicePatternIncludeTransLink() {

    ServicePattern pattern = _dao.getActiveServicePatternById(20065058);
    List<StopLocation> stops = _dao.getStopLocationsByServicePattern(pattern,
        true);
    assertEquals(1, stops.size());
    StopLocation stop = stops.get(0);
    assertEquals(25150, stop.getId());
    assertTrue(Hibernate.isInitialized(stop.getMainStreetName()));
    assertTrue(Hibernate.isInitialized(stop.getCrossStreetName()));
    TransLink link = stop.getTransLink();
    assertTrue(Hibernate.isInitialized(link));
    assertEquals(38637, link.getId());
    assertFalse(Hibernate.isInitialized(link.getTransNodeFrom()));
    assertFalse(Hibernate.isInitialized(link.getTransNodeTo()));
    /**
     * I original included this test, but the StreetName object is always
     * initialized because it's the exact same StreetName object already eagerly
     * loaded by the StopLocation
     */
    // assertFalse(Hibernate.isInitialized(link.getStreetName()));
  }

  public void testGetStopLocationsAndPptFlagByServicePattern() {
    fail("Not yet implemented");
  }

  /*****************************************************************************
   * 
   ****************************************************************************/

  public void testGetAllRoutes() {
    fail("Not yet implemented");
  }

  public void testGetActiveRoutes() {
    fail("Not yet implemented");
  }

  public void testGetRoutesByChangeDate() {
    fail("Not yet implemented");
  }

  public void testGetRouteById() {
    fail("Not yet implemented");
  }

  public void testGetRouteByNumber() {
    fail("Not yet implemented");
  }

  public void testGetActiveRoutesByStopId() {
    fail("Not yet implemented");
  }

  public void testGetRoutesByChangeDateAndStopId() {
    fail("Not yet implemented");
  }

  public void testGetActiveRoutesByTimepointId() {
    fail("Not yet implemented");
  }

  public void testGetRoutesByChangeDateAndTimepointId() {
    fail("Not yet implemented");
  }

  /*****************************************************************************
   * 
   ****************************************************************************/

  @Test
  public void testGetServicePatternById() {

    ChangeDate cd = _dao.getCurrentServiceRevision();
    ServicePatternKey key = new ServicePatternKey(cd, 11030002);

    ServicePattern pattern = _dao.getServicePatternById(key);

    assertNotNull(pattern);
    assertEquals(key, pattern.getId());
    assertEquals("NORTH", pattern.getDirection());
    assertEquals("S", pattern.getPatternType());
    assertEquals(2, pattern.getSchedulePatternId());
    assertEquals("L", pattern.getServiceType());

    assertTrue(Hibernate.isInitialized(pattern.getRoute()));

    assertEquals(30, pattern.getRoute().getNumber());
  }

  @Test
  public void testGetActiveServicePatternById() {

    int id = 20065058;
    ServicePattern pattern = _dao.getActiveServicePatternById(id);

    ChangeDate cd = _dao.getCurrentServiceRevision();
    ServicePatternKey key = new ServicePatternKey(cd, id);
    assertEquals(key, pattern.getId());

    assertEquals("SOUTH", pattern.getDirection());
    assertEquals("S", pattern.getPatternType());
    assertEquals(58, pattern.getSchedulePatternId());
    assertEquals("L", pattern.getServiceType());

    assertTrue(Hibernate.isInitialized(pattern.getRoute()));

    assertEquals(65, pattern.getRoute().getNumber());
  }

  @Test
  public void testGetAllServicePatterns() {
    List<ServicePattern> patterns = _dao.getAllServicePatterns();
    assertEquals(2, patterns.size());
    for (ServicePattern pattern : patterns)
      assertTrue(Hibernate.isInitialized(pattern.getRoute()));
  }

  @Test
  public void testGetActiveServicePatterns() {
    ChangeDate current = _dao.getCurrentServiceRevision();
    List<ServicePattern> patterns = _dao.getActiveServicePatterns();
    assertEquals(2, patterns.size());
    for (ServicePattern pattern : patterns) {
      assertEquals(current, pattern.getId().getChangeDate());
      assertTrue(Hibernate.isInitialized(pattern.getRoute()));
    }
  }

  @Test
  public void testGetActiveServicePatternsByRoute() {
    Route route = _dao.getRouteByNumber(30);
    List<ServicePattern> patterns = _dao.getActiveServicePatternsByRoute(route);
    assertEquals(1, patterns.size());
    ServicePattern pattern = patterns.get(0);
    assertEquals(11030002, pattern.getId().getId());
    assertEquals(_dao.getCurrentServiceRevision(),
        pattern.getId().getChangeDate());
    assertTrue(Hibernate.isInitialized(pattern.getRoute()));
    assertEquals(route, pattern.getRoute());
  }

  @Test
  public void testGetServicePatternsByChangeDateAndRoute() {
    ChangeDate cd = _dao.getCurrentServiceRevision();
    Route route = _dao.getRouteByNumber(30);
    List<ServicePattern> patterns = _dao.getServicePatternsByChangeDateAndRoute(
        cd, route);
    assertEquals(1, patterns.size());
    ServicePattern pattern = patterns.get(0);
    assertEquals(11030002, pattern.getId().getId());
    assertEquals(cd, pattern.getId().getChangeDate());
    assertTrue(Hibernate.isInitialized(pattern.getRoute()));
    assertEquals(route, pattern.getRoute());
  }

  /****
   * 
   ****/

  public void testGetActiveServicePatternTimeBlocksByRoute() {
    fail("Not yet implemented");
  }

  public void testGetServicePatternTimeBlocksByRoute() {
    fail("Not yet implemented");
  }

  public void testGetActiveSegmentedServicePatternTimeBlocksByRoute() {
    fail("Not yet implemented");
  }

  public void testGetSegmentedServicePatternTimeBlocksByRoute() {
    fail("Not yet implemented");
  }

  public void testGetTripById() {
    fail("Not yet implemented");
  }

  public void testGetTripsByServicePattern() {
    fail("Not yet implemented");
  }

  public void testGetTimepointById() {
    fail("Not yet implemented");
  }

  public void testGetTimepointsById() {
    fail("Not yet implemented");
  }

  public void testGetTimepointsByLocation() {
    fail("Not yet implemented");
  }

  public void testGetTimepointsByServicePattern() {
    fail("Not yet implemented");
  }

  public void testGetAllTimepoints() {
    fail("Not yet implemented");
  }

  public void testGetTimepointsWithFrequency() {
    fail("Not yet implemented");
  }

  public void testGetPatternTimepointsByServicePattern() {
    fail("Not yet implemented");
  }

  public void testGetPatternTimepointsAndTPIPathByServicePattern() {
    fail("Not yet implemented");
  }

  public void testGetTPIById() {
    fail("Not yet implemented");
  }

  public void testGetTransLinkById() {
    fail("Not yet implemented");
  }

  public void testGetTransLinkShapePointsByServicePattern() {
    fail("Not yet implemented");
  }

  public void testGetTransLinkShapePointsByTransLink() {
    fail("Not yet implemented");
  }

  public void testGetStreetNameByStopId() {
    fail("Not yet implemented");
  }

  public void testGetStopTimesByServicePattern() {
    fail("Not yet implemented");
  }

  public void testGetStopTimesByServicePatternTimeBlock() {
    fail("Not yet implemented");
  }

  public void testGetPassingTimesByServicePatternTimeBlock() {
    fail("Not yet implemented");
  }

  public void testGetActiveStopTimesByTimepointAndTimeRange() {
    fail("Not yet implemented");
  }

  public void testGetStopTimesByServiceRevisionAndTimepointAndTimeRange() {
    fail("Not yet implemented");
  }

  public void testGetActiveStopTimesByTimepointsAndTimeRange() {
    fail("Not yet implemented");
  }

  public void testGetStopTimesByServiceRevisionAndTimepointsAndTimeRange() {
    fail("Not yet implemented");
  }

  public void testGetStopTimesByServicePatternsAndTimeRange() {
    fail("Not yet implemented");
  }

  public void testGetInterpolatedStopTimesByChangeDateAndTimeRange() {
    fail("Not yet implemented");
  }

  public void testGetActiveInterpolatedStopTimesByTimeRange() {
    fail("Not yet implemented");
  }

  public void testGetInterpolatedStopTimesByChangeDateAndStopAndTimeRange() {
    fail("Not yet implemented");
  }

  public void testGetStopTimeInterpolationByServicePattern() {
    fail("Not yet implemented");
  }

  public void testGetStopTimeInterpolationByServicePatterns() {
    fail("Not yet implemented");
  }

  public void testGetStopTimeInterpolationByStops() {
    fail("Not yet implemented");
  }

  public void testGetActiveInterpolatedStopTimesByStopAndTimeRange() {
    fail("Not yet implemented");
  }

  public void testGetInterpolatedStopTimesFrequencyCountsByChangeDate() {
    fail("Not yet implemented");
  }

  public void testGetTimepointPairs() {
    fail("Not yet implemented");
  }

  public void testGetRegionsByLocation() {
    fail("Not yet implemented");
  }

  public void testGetStopsAndRegionsByServicePattern() {
    fail("Not yet implemented");
  }

  public void testGetBookmarksByUserId() {

  }
}
