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
package edu.washington.cs.rse.transit.web.oba.common.server;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;
import edu.washington.cs.rse.transit.common.MetroKCDAO;
import edu.washington.cs.rse.transit.common.model.Intersection;
import edu.washington.cs.rse.transit.common.model.Route;
import edu.washington.cs.rse.transit.common.model.ServicePattern;
import edu.washington.cs.rse.transit.common.model.StopLocation;
import edu.washington.cs.rse.transit.common.model.Trip;
import edu.washington.cs.rse.transit.common.model.aggregate.ScheduledArrivalTime;
import edu.washington.cs.rse.transit.common.model.aggregate.SelectionName;
import edu.washington.cs.rse.transit.common.model.aggregate.ServicePatternBlock;
import edu.washington.cs.rse.transit.common.model.aggregate.ServicePatternBlockKey;
import edu.washington.cs.rse.transit.web.oba.common.client.model.NameBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.PredictedArrivalBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.RouteBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.ServicePatternBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.ServicePatternBlockBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.StopAreaBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.StopBean;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import org.springframework.beans.factory.annotation.Autowired;

public class ApplicationBeanSupport {

  protected MetroKCDAO _dao;

  @Autowired
  public void setMetroKCDAO(MetroKCDAO dao) {
    _dao = dao;
  }

  protected StopBean getStopAsBean(StopLocation stop) {

    Point p = stop.getOffsetLocation();
    CoordinatePoint p2 = _dao.getPointAsLatLong(p);

    StopBean sb = new StopBean();
    sb.setId(Integer.toString(stop.getId()));
    sb.setLat(p2.getLat());
    sb.setLon(p2.getLon());
    sb.setDirection(stop.getDirection());
    sb.setName(getStopName(stop));

    return sb;
  }

  private String getStopName(StopLocation stop) {

    Intersection intersection = stop.getIntersection();
    String main = intersection.getMainStreet().getCombinedName();
    String cross = intersection.getCrossStreet().getCombinedName();

    boolean noMain = "Unnamed".equals(main);
    boolean noCross = "Unnamed".equals(cross);

    if (noMain && noCross)
      return "Stop # " + stop.getId();
    else if (noMain)
      return cross;
    else if (noCross)
      return main;
    else
      return main + " & " + cross;

  }

  protected StopAreaBean getStopAreaAsBean(StopLocation stop) {

    StopBean sib = getStopAsBean(stop);

    StopAreaBean bean = new StopAreaBean();
    bean.setStopAndIntersectionBean(sib);

    Geometry envelope = stop.getLocation().buffer(300).getEnvelope();
    for (StopLocation nearby : _dao.getStopLocationsByLocation(envelope)) {
      if (!nearby.equals(stop)) {
        StopBean nearbyBean = getStopAsBean(nearby);
        bean.addNearbyStop(nearbyBean);
      }
    }
    return bean;
  }

  protected NameBean getNameAsBean(SelectionName name) {
    return new NameBean(name.getType(), name.getNames());
  }

  protected PredictedArrivalBean getPredictedArrivalTimeAsBean(
      ScheduledArrivalTime sat) {

    Route route = sat.getRoute();
    ServicePattern sp = sat.getServicePattern();
    Trip trip = sat.getTrip();

    PredictedArrivalBean pab = new PredictedArrivalBean();

    pab.setScheduledTime(sat.getScheduledTime());

    if (sat.hasPredictedTime())
      pab.setPredictedTime(sat.getPredictedTime());

    String routeName = Integer.toString(route.getNumber());
    if (sp.isExpress())
      routeName += "E";
    pab.setRoute(routeName);
    pab.setDestination(sp.getGeneralDestination());
    pab.setTripId(Integer.toString(trip.getId()));

    return pab;
  }

  protected RouteBean getRouteAsBean(Route route) {
    RouteBean bean = new RouteBean();
    bean.setId(Integer.toString(route.getId()));
    bean.setNumber(Integer.toString(route.getNumber()));
    return bean;
  }

  protected ServicePatternBean getServicePatternAsBean(ServicePattern pattern) {

    ServicePatternBean bean = new ServicePatternBean();
    bean.setChangeDate(pattern.getId().getChangeDate().getId());
    bean.setId(pattern.getId().getId());
    bean.setExpress(pattern.isExpress());
    bean.setGeneralDestination(pattern.getGeneralDestination());
    bean.setSpecificDestination(pattern.getSpecificDestination());

    return bean;
  }

  protected ServicePatternBlockBean getServicePatternBlockAsBean(
      ServicePatternBlock block) {

    ServicePatternBlockBean bean = new ServicePatternBlockBean();
    ServicePatternBlockKey id = block.getId();
    bean.setId(id.getId());
    bean.setRoute(Integer.toString(id.getRoute().getNumber()));
    bean.setDescription(block.getDescription());
    return bean;
  }
}
