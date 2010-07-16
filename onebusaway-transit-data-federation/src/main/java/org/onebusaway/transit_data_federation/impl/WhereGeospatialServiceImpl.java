package org.onebusaway.transit_data_federation.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data_federation.impl.tripplanner.DistanceLibrary;
import org.onebusaway.transit_data_federation.services.beans.GeospatialBeanService;
import org.onebusaway.transit_data_federation.services.beans.RouteBeanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.ItemVisitor;
import com.vividsolutions.jts.index.strtree.STRtree;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;
import edu.washington.cs.rse.geospatial.latlon.CoordinateRectangle;

@Component
class WhereGeospatialServiceImpl implements GeospatialBeanService {

  @Autowired
  private GtfsRelationalDao _dao;

  private volatile boolean _initialized = false;

  private STRtree _tree;

  @PostConstruct
  public void initialize() {

    if (!_initialized) {

      synchronized (this) {

        if (!_initialized) {

          Collection<Stop> stops = _dao.getAllStops();

          _tree = new STRtree(stops.size());

          for (Stop stop : stops) {
            float x = (float) stop.getLon();
            float y = (float) stop.getLat();
            Envelope env = new Envelope(x, x, y, y);
            _tree.insert(env, stop.getId());
          }

          _tree.build();
          _initialized = true;
        }
      }
    }

  }

  /****
   * {@link RouteBeanService} Interface
   ****/

  public List<AgencyAndId> getStopsByLocation(double lat, double lon,
      double radius) {
    CoordinateRectangle bounds = DistanceLibrary.bounds(new CoordinatePoint(
        lat, lon), radius);
    return getStopsByBounds(bounds.getMinLat(), bounds.getMinLon(),
        bounds.getMaxLat(), bounds.getMaxLon());
  }

  public List<AgencyAndId> getStopsByBounds(double lat1, double lon1,
      double lat2, double lon2) {

    initialize();

    float xMin = (float) Math.min(lon1, lon2);
    float yMin = (float) Math.min(lat1, lat2);
    float xMax = (float) Math.max(lon1, lon2);
    float yMax = (float) Math.max(lat1, lat2);

    TreeVisistor v = new TreeVisistor();
    _tree.query(new Envelope(xMin,xMax,yMin,yMax), v);
    return v.getIdsInRange();
  }

  private class TreeVisistor implements ItemVisitor {

    private List<AgencyAndId> _idsInRange = new ArrayList<AgencyAndId>();

    public List<AgencyAndId> getIdsInRange() {
      return _idsInRange;
    }

    @Override
    public void visitItem(Object obj) {
      _idsInRange.add((AgencyAndId) obj);
    }
  }

}
