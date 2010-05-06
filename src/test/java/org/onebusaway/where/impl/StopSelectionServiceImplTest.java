package org.onebusaway.where.impl;

import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.where.model.SelectionName;
import org.onebusaway.where.model.StopSelectionTree;
import org.onebusaway.where.services.NoSuchRouteException;
import org.onebusaway.where.services.StopSelectionService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations = {
    "/data-sources-common.xml", "/data-sources-server.xml",
    "/org/onebusaway/application-context-common.xml",
    "/org/onebusaway/application-context-server.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class StopSelectionServiceImplTest {

  @Autowired
  private GtfsDao _dao;

  @Autowired
  private StopSelectionService _stopSelectionServiceImpl;

  @Test
  public void test() throws NoSuchRouteException {
    for (Route route : _dao.getAllRoutes()) {
      System.err.println("route=" + route.getShortName());
      _stopSelectionServiceImpl.getStopsByRoute(route.getShortName());
    }
  }

  public void go(StopSelectionTree tree, String prefix) {
    if (tree.hasStop()) {
      System.out.println(prefix + "stop=" + tree.getStop().getName());
      return;
    }
    for (SelectionName name : tree.getNames()) {
      System.out.println(prefix + name.getName());
      go(tree.getSubTree(name), prefix + "  ");
    }
  }

}
