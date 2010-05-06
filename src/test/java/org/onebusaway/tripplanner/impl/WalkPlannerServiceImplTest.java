package org.onebusaway.tripplanner.impl;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

import com.vividsolutions.jts.geom.Point;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.onebusaway.WebTestContextLoader;
import org.onebusaway.common.services.ProjectionService;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.tripplanner.model.WalkPlan;
import org.onebusaway.tripplanner.services.NoPathException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(loader = WebTestContextLoader.class, locations = {
    "/data-sources-common.xml", "/data-sources-server.xml",
    "/org/onebusaway/application-context-common.xml",
    "/org/onebusaway/application-context-server.xml",
    "/org/onebusaway/tripplanner/application-context-common.xml",
    "/org/onebusaway/tripplanner/application-context-server.xml",
    "/org/onebusaway/oba/application-context-common.xml",
    "/org/onebusaway/oba/application-context-server.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class WalkPlannerServiceImplTest {

  @Autowired
  GtfsDao _dao;

  @Autowired
  private WalkPlannerServiceImpl _service;

  @Autowired
  private ProjectionService _projection;

  @Test
  public void test() throws NoPathException {

    Stop a = _dao.getStopById("10020");
    Stop b = _dao.getStopById("10030");

    CoordinatePoint c = new CoordinatePoint(47.669799, -122.289434);
    Point p = _projection.getCoordinatePointAsPoint(c);
    WalkPlan planA = _service.getWalkPlan(p, a.getLocation());
    WalkPlan planB = _service.getWalkPlan(p, b.getLocation());

    System.out.println("a=" + planA.getDistance());
    System.out.println("b=" + planB.getDistance());
  }
}
