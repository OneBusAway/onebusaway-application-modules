package org.onebusaway.transit_data_federation.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data_federation.services.beans.GeospatialBeanService;
import org.onebusaway.transit_data_federation.services.beans.RouteBeanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.ItemVisitor;
import com.vividsolutions.jts.index.strtree.STRtree;

@Component
class WhereGeospatialServiceImpl implements GeospatialBeanService {

  private GtfsRelationalDao _dao;

  private volatile boolean _initialized = false;

  private STRtree _tree;
  
  @Autowired
  public void setGtfsRelationalDao(GtfsRelationalDao dao) {
    _dao = dao;
  }

  @PostConstruct
  public void initialize() {

    if (!_initialized) {

      synchronized (this) {

        if (!_initialized) {

          Collection<Stop> stops = _dao.getAllStops();
          if (stops.size() == 0) {
            throw new IllegalStateException("No stops in database.");
          }
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

  @Override
  public List<AgencyAndId> getStopsByBounds(CoordinateBounds bounds) {
    
    initialize();

    double xMin = bounds.getMinLon();
    double yMin = bounds.getMinLat();
    double xMax = bounds.getMaxLon();
    double yMax = bounds.getMaxLat();

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
