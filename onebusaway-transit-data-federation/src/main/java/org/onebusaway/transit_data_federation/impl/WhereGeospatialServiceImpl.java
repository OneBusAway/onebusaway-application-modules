package org.onebusaway.transit_data_federation.impl;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data_federation.impl.tripplanner.DistanceLibrary;
import org.onebusaway.transit_data_federation.services.beans.GeospatialBeanService;
import org.onebusaway.transit_data_federation.services.beans.RouteBeanService;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;
import edu.washington.cs.rse.geospatial.latlon.CoordinateRectangle;

import com.infomatiq.jsi.IntProcedure;
import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.rtree.RTree;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;

@Component
class WhereGeospatialServiceImpl implements GeospatialBeanService {

  @Autowired
  private GtfsRelationalDao _dao;

  private volatile boolean _initialized = false;

  private List<AgencyAndId> _ids;

  private RTree _tree;

  @PostConstruct
  public void initialize() {

    if (!_initialized) {

      synchronized (this) {

        if (!_initialized) {

          Collection<Stop> stops = _dao.getAllStops();
          _ids = new ArrayList<AgencyAndId>();

          _tree = new RTree();
          _tree.init(new Properties());

          for (Stop stop : stops) {
            int index = _ids.size();
            _ids.add(stop.getId());
            float x = (float) stop.getLon();
            float y = (float) stop.getLat();
            _tree.add(new Rectangle(x, y, x, y), index);
          }

          _initialized = true;
        }
      }
    }

  }

  /****
   * {@link RouteBeanService} Interface
   ****/

  public List<AgencyAndId> getStopsByLocation(double lat, double lon, double radius) {
    CoordinateRectangle bounds = DistanceLibrary.bounds(new CoordinatePoint(lat, lon), radius);
    return getStopsByBounds(bounds.getMinLat(),bounds.getMinLon(),bounds.getMaxLat(),bounds.getMaxLon());
  }
  
  public List<AgencyAndId> getStopsByBounds(double lat1, double lon1,
      double lat2, double lon2) {

    initialize();

    float xMin = (float) Math.min(lon1,lon2);
    float yMin = (float) Math.min(lat1,lat2);
    float xMax = (float) Math.max(lon1,lon2);
    float yMax = (float) Math.max(lat1,lat2);

    TreeVisistor v = new TreeVisistor();
    _tree.contains(new Rectangle(xMin, yMin, xMax, yMax), v);
    return v.getIdsInRange();
  }

  private class TreeVisistor implements IntProcedure {

    private List<AgencyAndId> _idsInRange = new ArrayList<AgencyAndId>();

    public List<AgencyAndId> getIdsInRange() {
      return _idsInRange;
    }

    public boolean execute(int index) {
      _idsInRange.add(_ids.get(index));
      return true;
    }

  }


}
