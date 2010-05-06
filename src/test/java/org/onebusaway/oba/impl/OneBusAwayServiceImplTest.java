package org.onebusaway.oba.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.onebusaway.MyTestRunner;
import org.onebusaway.WebTestContextLoader;
import org.onebusaway.common.web.common.client.rpc.ServiceException;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.oba.web.common.client.model.OneBusAwayConstraintsBean;
import org.onebusaway.tripplanner.offline.StopEntryImpl;
import org.onebusaway.tripplanner.services.TripPlannerGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.ParseException;
import java.util.Date;

@ContextConfiguration(loader = WebTestContextLoader.class, locations = {
    "/data-sources-common.xml", "/data-sources-server.xml",
    "/org/onebusaway/application-context-common.xml",
    "/org/onebusaway/application-context-server.xml",
    "/org/onebusaway/tripplanner/application-context-common.xml",
    "/org/onebusaway/tripplanner/application-context-server.xml",
    "/org/onebusaway/oba/application-context-common.xml",
    "/org/onebusaway/oba/application-context-server.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class OneBusAwayServiceImplTest {

  @Autowired
  private OneBusAwayWebServiceImpl _service;

  @Autowired
  private TripPlannerGraph _graph;

  public static void main(String[] args) throws ServiceException,
      ParseException {
    OneBusAwayServiceImplTest oba = MyTestRunner.create(OneBusAwayServiceImplTest.class);
    oba.test();
    System.exit(0);
  }

  @Test
  public void test() throws ServiceException, ParseException {

    long t = System.currentTimeMillis();
    int i = 0;
    for (String id : _graph.getStopIds()) {
      StopEntryImpl entry = _graph.getStopEntryByStopId(id);
      Stop stop = entry.getStop();
      double lat = stop.getLat() + (Math.random() - 0.5) * 0.001;
      double lon = stop.getLon() + (Math.random() - 0.5) * 0.001;

      if (i % 10 == 0)
        System.out.println("i=" + i);
      i++;

      if (i == 1000)
        break;

      OneBusAwayConstraintsBean constraints = new OneBusAwayConstraintsBean();
      constraints.setMaxTransfers(1);
      constraints.setMaxWalkingDistance(2640.0);
      constraints.setMaxTripDuration(15);
      constraints.setMinDepartureTime(new Date(1227330720000L));
      constraints.setSearchWindow(15);

      try {
        _service.getStops(lat, lon, constraints);
      } catch (Exception ex) {
        System.err.println("================ STOP=" + id
            + " ==================");
      }
      // _service.getStops(47.669799, -122.289434, constraints);
    }

    System.out.println("total=" + (System.currentTimeMillis() - t));

  }
}
