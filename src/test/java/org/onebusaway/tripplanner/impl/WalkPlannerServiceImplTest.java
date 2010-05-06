package org.onebusaway.tripplanner.impl;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

import edu.emory.mathcs.backport.java.util.Collections;

import com.vividsolutions.jts.geom.Point;

import org.junit.Test;
import org.onebusaway.BaseTest;
import org.onebusaway.common.services.ProjectionService;
import org.onebusaway.tripplanner.NoPathException;
import org.onebusaway.tripplanner.model.Walk;
import org.onebusaway.tripplanner.model.WalkNode;
import org.onebusaway.tripplanner.model.WalkPlannerGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

public class WalkPlannerServiceImplTest {

  @Autowired
  private WalkPlannerServiceImpl _service;

  @Autowired
  private ProjectionService _projection;

  @Autowired
  private ApplicationContext _context;

  public static void main(String[] args) {
    WalkPlannerServiceImplTest test = new WalkPlannerServiceImplTest();

    ApplicationContext context = BaseTest.getContext();

    context.getAutowireCapableBeanFactory().autowireBean(test);
    test.test02();
  }

  @Test
  public void test() {

    WalkPlannerGraph graph = (WalkPlannerGraph) _context.getBean("walkPlannerGraph");
    List<Integer> ids = new ArrayList<Integer>(graph.getIds());

    Collections.shuffle(ids);

    for (int j = 0; j < 60; j++) {
      System.out.println("j=" + j);
      for (int i = 0; i + 1 < ids.size(); i += 2) {
        Point from = graph.getLocationById(ids.get(i));
        Point to = graph.getLocationById(ids.get(i + 1));
        try {
          _service.getWalkPlan(from, to);
        } catch (NoPathException ex) {

        }
      }
    }
  }

  @Test
  public void test02() {
    Point pFrom = _projection.getLatLonAsPoint(47.66107359434927,
        -122.29861378669739);
    Point pTo = _projection.getLatLonAsPoint(47.66496827950675,
        -122.30672478675842);
    try {
      long tIn = System.currentTimeMillis();
      Walk walk = _service.getWalkPlan(pFrom, pTo);
      long tOut = System.currentTimeMillis();
      System.out.println("distance=" + walk.getDistance() + " t="
          + (tOut - tIn));
      for (WalkNode node : walk) {
        CoordinatePoint p = node.getLatLon();
        System.out.println(p.getLat() + " " + p.getLon());
      }
    } catch (NoPathException e) {
      e.printStackTrace();
    }
  }
}
