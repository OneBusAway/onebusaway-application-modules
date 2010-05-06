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
package edu.washington.cs.rse.transit.web.actions;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;
import edu.washington.cs.rse.transit.common.MetroKCDAO;
import edu.washington.cs.rse.transit.common.model.StopLocation;
import edu.washington.cs.rse.transit.web.oba.common.client.model.StopBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.StopsBean;

public class StopsByLocationAndAccuracyAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  private MetroKCDAO _dao;

  private double _lat;

  private double _lon;

  private int _accuracy;

  private StopsBean _stops;

  @Autowired
  public void setMetroKCDAO(MetroKCDAO dao) {
    _dao = dao;
  }

  public void setLat(double lat) {
    _lat = lat;
  }

  public void setLon(double lon) {
    _lon = lon;
  }

  public void setAccuracy(int accuracy) {
    _accuracy = accuracy;
  }

  public StopsBean getStops() {
    return _stops;
  }

  public String execute() throws Exception {

    int r = GeocoderAccuracyToBounds.getBoundsInFeetByAccuracy(_accuracy);
    Geometry envelope = _dao.getLatLonAsPoint(_lat, _lon);
    envelope = envelope.buffer(r).getEnvelope();

    List<StopLocation> stops = _dao.getStopLocationsByLocation(envelope);

    List<StopBean> stopBeans = new ArrayList<StopBean>();

    for (StopLocation stop : stops) {
      StopBean sb = new StopBean();
      sb.setId(Integer.toString(stop.getId()));
      Point p = stop.getLocation();
      CoordinatePoint p2 = _dao.getPointAsLatLong(p);
      sb.setLat(p2.getLat());
      sb.setLon(p2.getLon());
      stopBeans.add(sb);
    }

    _stops = new StopsBean();
    _stops.setStopBeans(stopBeans);

    return SUCCESS;
  }
}
