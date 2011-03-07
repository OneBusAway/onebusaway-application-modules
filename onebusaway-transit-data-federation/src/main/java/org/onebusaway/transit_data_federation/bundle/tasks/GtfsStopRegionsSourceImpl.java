/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.onebusaway.transit_data_federation.bundle.tasks;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsDao;
import org.opentripplanner.graph_builder.services.RegionsSource;
import org.springframework.beans.factory.annotation.Autowired;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public class GtfsStopRegionsSourceImpl implements RegionsSource {

  private GtfsDao _gtfsDao;

  private double _radius = 2500;

  private double _latRadius = 0;

  private double _lonRadius = 0;

  @Autowired
  public void setGtfsDao(GtfsDao dao) {
    _gtfsDao = dao;
  }

  /**
   * 
   * @param radiusInMeters - radius in meters
   */
  public void setRadius(double radiusInMeters) {
    _radius = radiusInMeters;
  }

  @Override
  public Iterable<Envelope> getRegions() {

    List<Envelope> regions = new ArrayList<Envelope>();

    for (Stop stop : _gtfsDao.getAllStops()) {

      if (_latRadius == 0 || _lonRadius == 0) {
        CoordinateBounds bounds = SphericalGeometryLibrary.bounds(
            stop.getLat(), stop.getLon(), _radius);
        _latRadius = (bounds.getMaxLat() - bounds.getMinLat()) / 2;
        _lonRadius = (bounds.getMaxLon() - bounds.getMinLon()) / 2;
      }

      Coordinate a = new Coordinate(stop.getLon() - _lonRadius, stop.getLat()
          - _latRadius);
      Coordinate b = new Coordinate(stop.getLon() + _lonRadius, stop.getLat()
          + _latRadius);
      Envelope env = new Envelope(a, b);
      regions.add(env);
    }

    return regions;
  }
}
