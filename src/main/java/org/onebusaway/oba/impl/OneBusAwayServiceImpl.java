package org.onebusaway.oba.impl;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

import org.onebusaway.gtdf.model.Stop;
import org.onebusaway.oba.web.standard.client.model.TimedStopBean;
import org.onebusaway.oba.web.standard.client.rpc.OneBusAwayService;
import org.onebusaway.tripplanner.TripPlannerService;
import org.onebusaway.where.impl.ApplicationBeanSupport;
import org.onebusaway.where.web.common.client.model.StopBean;
import org.onebusaway.where.web.common.client.rpc.ServiceException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OneBusAwayServiceImpl extends ApplicationBeanSupport implements
    OneBusAwayService {

  private TripPlannerService _tripPlannerService;

  public void setTripPlannerService(TripPlannerService tripPlannerService) {
    _tripPlannerService = tripPlannerService;
  }

  public List<TimedStopBean> getStops(double lat, double lon)
      throws ServiceException {
    CoordinatePoint p = new CoordinatePoint(lat, lon);
    long from = System.currentTimeMillis();
    long to = from + 15 * 60 * 1000;
    Map<Stop, Long> trips = _tripPlannerService.getTrips(p, from, to);
    List<TimedStopBean> beans = new ArrayList<TimedStopBean>(trips.size());
    for (Map.Entry<Stop, Long> entry : trips.entrySet()) {
      Stop stop = entry.getKey();
      Long time = entry.getValue();
      int ellapsed = (int) ((time - from) / 1000);
      StopBean stopBean = getStopAsBean(stop);
      beans.add(new TimedStopBean(stopBean, ellapsed));
    }
    return beans;
  }
}
