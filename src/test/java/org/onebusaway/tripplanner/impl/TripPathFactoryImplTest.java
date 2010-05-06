package org.onebusaway.tripplanner.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.onebusaway.common.web.common.client.model.PathBean;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.services.GtfsDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations = {
    "/data-sources-common.xml", "/data-sources-server.xml",
    "/org/onebusaway/application-context-common.xml",
    "/org/onebusaway/application-context-server.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class TripPathFactoryImplTest {

  @Autowired
  private GtfsDao _dao;

  @Test
  public void go() {
    StopTime from = _dao.getStopTimeById(34947);
    StopTime to = _dao.getStopTimeById(34968);

    TripPathFactoryImpl factory = new TripPathFactoryImpl();
    factory.setGtfsDao(_dao);
    PathBean path = factory.getStopTimesAsPath(from, to);
    double[] lat = path.getLat();
    double[] lon = path.getLon();
    for (int i = 0; i < lat.length; i++) {
      System.out.println(lat[i] + " " + lon[i]);
    }

  }
}
