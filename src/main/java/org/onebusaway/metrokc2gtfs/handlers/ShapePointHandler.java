package org.onebusaway.metrokc2gtfs.handlers;

import edu.washington.cs.rse.collections.CollectionsLibrary;
import edu.washington.cs.rse.collections.FactoryMap;
import edu.washington.cs.rse.collections.IndexedSet;
import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

import org.onebusaway.common.services.ProjectionService;
import org.onebusaway.csv.CsvEntityWriter;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.ShapePointKey;
import org.onebusaway.metrokc2gtfs.TranslationContext;
import org.onebusaway.metrokc2gtfs.model.MetroKCPatternTimepoint;
import org.onebusaway.metrokc2gtfs.model.MetroKCTPIPath;
import org.onebusaway.metrokc2gtfs.model.ServicePatternKey;

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

  public void writeShapes() {

    PatternTimepointsHandler _ptHandler = _context.getHandler(PatternTimepointsHandler.class);
    CsvEntityWriter writer = _context.getWriter();

    Map<ServicePatternKey, List<MetroKCPatternTimepoint>> data = _ptHandler.getData();

    ShapePoint sp = new ShapePoint();

    for (ServicePatternKey servicePatternId : data.keySet()) {

      String id = getShapeId(servicePatternId);
      int index = 0;

      double prevX = 0;
      double prevY = 0;
      double distanceTraveled = 0.0;
      MetroKCShapePoint lastPoint = null;

      List<MetroKCPatternTimepoint> pts = data.get(servicePatternId);
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

          for (MetroKCShapePoint s : points) {

            if (index == 0) {
              prevX = s.getX();
              prevY = s.getY();
            }

            double dx = s.getX() - prevX;
            double dy = s.getY() - prevY;

            distanceTraveled += Math.sqrt(dx * dx + dy * dy);

            sp.setId(new ShapePointKey(id, index++));
            sp.setDistTraveled(distanceTraveled);
            sp.setLat(s.getLat());
            sp.setLon(s.getLon());

            writer.handleEntity(sp);
            prevX = s.getX();
            prevY = s.getY();
            lastPoint = s;
          }
        }
      }

      _lastPointByServicePattern.put(servicePatternId, lastPoint);
    }

    // TODO : turn this back on after debugging
    //_shapesByTransLinkId.clear();
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
  
  public List<MetroKCShapePoint> getShapePointsByTransLinkId(int id){
    return _shapesByTransLinkId.get(id);
  }

  public MetroKCShapePoint getLastShapePointByServicePattern(
      ServicePatternKey id) {
    return _lastPointByServicePattern.get(id);
  }
}
