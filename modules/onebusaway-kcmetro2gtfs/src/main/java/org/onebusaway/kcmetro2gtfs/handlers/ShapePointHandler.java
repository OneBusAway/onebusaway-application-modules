package org.onebusaway.kcmetro2gtfs.handlers;

import org.onebusaway.gtfs.csv.CsvEntityWriter;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.kcmetro2gtfs.TranslationContext;
import org.onebusaway.kcmetro2gtfs.model.MetroKCPatternTimepoint;
import org.onebusaway.kcmetro2gtfs.model.MetroKCShapePoint;
import org.onebusaway.kcmetro2gtfs.model.MetroKCTPIPath;
import org.onebusaway.kcmetro2gtfs.model.ServicePatternKey;
import org.onebusaway.kcmetro2gtfs.services.ProjectionService;

import edu.washington.cs.rse.collections.CollectionsLibrary;
import edu.washington.cs.rse.collections.FactoryMap;
import edu.washington.cs.rse.collections.IndexedSet;
import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShapePointHandler extends InputHandler {

  private static final String[] TRANS_LINK_SHAPE_POINT_FIELDS = {
      "trans_link_id", "sequence", "dbModDate", "x", "y"};

  private TranslationContext _context;

  private TPIPathHandler _tpipHandler;

  private ProjectionService _projection;

  private IndexedSet<ServicePatternKey> _ids = new IndexedSet<ServicePatternKey>();

  private Map<Integer, List<MetroKCShapePoint>> _shapesByTransLinkId = new FactoryMap<Integer, List<MetroKCShapePoint>>(
      new ArrayList<MetroKCShapePoint>());

  private Map<ServicePatternKey, MetroKCShapePoint> _lastPointByServicePattern = new HashMap<ServicePatternKey, MetroKCShapePoint>();

  private Map<ServicePatternKey, Double> _maxShapeDistanceTraveled = new HashMap<ServicePatternKey, Double>();

  public ShapePointHandler(TranslationContext context) {
    super(MetroKCShapePoint.class, TRANS_LINK_SHAPE_POINT_FIELDS);
    _context = context;
    _projection = context.getProjectionService();
    _tpipHandler = context.getHandler(TPIPathHandler.class);
  }

  public String getShapeId(ServicePatternKey servicePatternId) {
    _ids.add(servicePatternId);
    return Integer.toString(_ids.getIndex(servicePatternId));
  }

  public void setMaxShapeDistanceTraveled(ServicePatternKey servicePatternId,
      double maxShapeDistanceTraveled) {
    Double value = _maxShapeDistanceTraveled.get(servicePatternId);
    if (value == null || value < maxShapeDistanceTraveled)
      _maxShapeDistanceTraveled.put(servicePatternId, maxShapeDistanceTraveled);
  }

  public void writeShapes() {

    CsvEntityWriter writer = _context.getWriter();

    ShapePoint sp = new ShapePoint();
    int shapePointId = 0;
    
    for (ServicePatternKey servicePatternId : _ids) {

      String id = getShapeId(servicePatternId);
      int index = 0;

      // Double v = _maxShapeDistanceTraveled.get(servicePatternId);
      // double maxDistanceTraveled = v == null ? 0 : v.doubleValue();

      double maxDistanceTraveled = _maxShapeDistanceTraveled.get(servicePatternId);

      double prevX = 0;
      double prevY = 0;
      double distanceTraveled = 0.0;
      MetroKCShapePoint lastPoint = null;

      List<MetroKCShapePoint> shapePoints = getShapePointsByServicePattern(servicePatternId);

      for (int i = 0; i < shapePoints.size(); i++) {
        MetroKCShapePoint s = shapePoints.get(i);

        if (index == 0) {
          prevX = s.getX();
          prevY = s.getY();
        }

        double dx = s.getX() - prevX;
        double dy = s.getY() - prevY;

        distanceTraveled += Math.sqrt(dx * dx + dy * dy);

        AgencyAndId shapeId = new AgencyAndId(_context.getAgencyId(), id);
        sp.setId(shapePointId++);
        sp.setShapeId(shapeId);
        sp.setSequence(index++);
        sp.setDistTraveled(distanceTraveled);
        sp.setLat(s.getLat());
        sp.setLon(s.getLon());

        // Last point?
        if (i == shapePoints.size() - 1) {
          if (distanceTraveled < maxDistanceTraveled
              && maxDistanceTraveled - distanceTraveled < 1)
            sp.setDistTraveled(maxDistanceTraveled);
        }

        writer.handleEntity(sp);

        prevX = s.getX();
        prevY = s.getY();
        lastPoint = s;
      }

      _lastPointByServicePattern.put(servicePatternId, lastPoint);
    }

    // _shapesByTransLinkId.clear();
  }

  public void handleEntity(Object bean) {

    MetroKCShapePoint shape = (MetroKCShapePoint) bean;
    int transLinkId = shape.getTransLinkId();

    if (!_tpipHandler.isTransLinkIdActive(transLinkId))
      return;

    CoordinatePoint latlon = _projection.getXYAsLatLong(shape.getX(),
        shape.getY());

    shape.setLat(latlon.getLat());
    shape.setLon(latlon.getLon());

    _shapesByTransLinkId.get(transLinkId).add(shape);
  }

  public List<MetroKCShapePoint> getShapePointsByTransLinkId(int id) {
    return _shapesByTransLinkId.get(id);
  }

  public List<MetroKCShapePoint> getShapePointsByServicePattern(
      ServicePatternKey id) {

    List<MetroKCShapePoint> shapePoints = new ArrayList<MetroKCShapePoint>();

    PatternTimepointsHandler _ptHandler = _context.getHandler(PatternTimepointsHandler.class);
    Map<ServicePatternKey, List<MetroKCPatternTimepoint>> data = _ptHandler.getData();

    List<MetroKCPatternTimepoint> pts = data.get(id);
    Collections.sort(pts);

    for (MetroKCPatternTimepoint pt : pts) {

      if (pt.getTpiId() == 0)
        continue;

      List<MetroKCTPIPath> tpips = _tpipHandler.getTPIPathsById(pt.getTpiId());
      Collections.sort(tpips);

      for (MetroKCTPIPath tpip : tpips) {

        if (!_shapesByTransLinkId.containsKey(tpip.getTransLink()))
          throw new IllegalStateException("no such transLink id="
              + tpip.getTransLink());

        List<MetroKCShapePoint> shapes = _shapesByTransLinkId.get(tpip.getTransLink());
        Collections.sort(shapes);

        Iterable<MetroKCShapePoint> points = shapes;

        if (tpip.getFlowDirection() > 0)
          points = CollectionsLibrary.getReverseIterable(shapes);

        for (MetroKCShapePoint point : points)
          shapePoints.add(point);
      }
    }

    return shapePoints;
  }

  public MetroKCShapePoint getLastShapePointByServicePattern(
      ServicePatternKey id) {

    return _lastPointByServicePattern.get(id);
  }

}
