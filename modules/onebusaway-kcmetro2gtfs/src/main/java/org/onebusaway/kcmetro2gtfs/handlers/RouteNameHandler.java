package org.onebusaway.kcmetro2gtfs.handlers;

import org.onebusaway.gtfs.model.Route;
import org.onebusaway.kcmetro2gtfs.TranslationContext;
import org.onebusaway.kcmetro2gtfs.impl.LocationNamingStrategy;
import org.onebusaway.kcmetro2gtfs.impl.MetroDao;
import org.onebusaway.kcmetro2gtfs.model.MetroKCServicePattern;
import org.onebusaway.kcmetro2gtfs.model.MetroKCShapePoint;
import org.onebusaway.kcmetro2gtfs.services.ProjectionService;
import org.onebusaway.layers.model.Layer;
import org.onebusaway.layers.model.Region;

import com.vividsolutions.jts.geom.Point;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class RouteNameHandler implements Runnable {

  private static final double SERVICE_PATTERN_TRIP_COUNT_RATIO_MIN = 0.2;

  private static final LayerAndRegionComparator LAYER_AND_REGION_COMPARATOR = new LayerAndRegionComparator();

  private MetroDao _dao;

  private ProjectionService _projection;

  private LocationNamingStrategy _locationNaming;

  private TranslationContext _context;

  public RouteNameHandler(TranslationContext context) {
    _context = context;
    _dao = context.getDao();
    _projection = context.getProjectionService();
    _locationNaming = context.getLocationNamingStrategy();
  }

  private Collection<Route> getRoutes() {
    return _dao.getAllRoutes();
  }

  public void run() {

    for (Route route : getRoutes()) {

      Set<MetroKCServicePattern> servicePatterns = _dao.getServicePatternsByRoute(route);

      long maxTripCount = 0;

      for (MetroKCServicePattern pattern : servicePatterns) {
        long tripCount = _dao.getTripCount(pattern);
        if (tripCount > maxTripCount)
          maxTripCount = tripCount;
      }

      Map<MetroKCServicePattern, SortedMap<Layer, Region>> regionsByServicePattern = new HashMap<MetroKCServicePattern, SortedMap<Layer, Region>>();

      for (MetroKCServicePattern pattern : servicePatterns) {
        long tripCount = _dao.getTripCount(pattern);
        if (tripCount > maxTripCount * SERVICE_PATTERN_TRIP_COUNT_RATIO_MIN) {
          regionsByServicePattern.put(pattern, getRegions(pattern));
        }
      }
      pruneCommonLayers(regionsByServicePattern);

      List<SortedMap<Layer, Region>> regions = new ArrayList<SortedMap<Layer, Region>>(
          regionsByServicePattern.values());
      Collections.sort(regions, LAYER_AND_REGION_COMPARATOR);

      String regionsAsName = getRegionsAsName(regions);
      route.setLongName(regionsAsName);
    }
  }

  private void pruneCommonLayers(
      Map<MetroKCServicePattern, SortedMap<Layer, Region>> regions2) {

    while (true) {

      Layer currentLayer = null;
      Region currentRegion = null;

      for (SortedMap<Layer, Region> regions : regions2.values()) {

        if (regions.isEmpty())
          throw new IllegalStateException("bad");
        if (regions.size() == 1)
          return;
        Layer layer = regions.firstKey();
        Region region = regions.get(layer);

        if (currentLayer == null) {
          currentLayer = layer;
          currentRegion = region;
        } else if (!layer.equals(currentLayer) || !region.equals(currentRegion))
          return;
      }

      // If we made it this far, then everyone has the same first layer and
      // region
      for (SortedMap<Layer, Region> regions : regions2.values())
        regions.remove(currentLayer);
    }
  }

  private SortedMap<Layer, Region> getRegions(
      MetroKCServicePattern servicePattern) {

    List<MetroKCShapePoint> shapePoints = _dao.getLastShapePointByServicePattern(servicePattern);

    for (int i = shapePoints.size() - 1; i >= 0; i--) {

      MetroKCShapePoint sp = shapePoints.get(i);
      Point location = _projection.getXYAsPoint(sp.getX(), sp.getY());
      SortedMap<Layer, Region> regions = _locationNaming.getRegionsByLocation(location);

      if (!regions.isEmpty()) {
        return regions;
      }
    }

    _context.addWarning("no regions for service pattern "
        + servicePattern.getId() + " stops=" + shapePoints.size());

    return new TreeMap<Layer, Region>();
  }

  private String getRegionsAsName(Collection<SortedMap<Layer, Region>> regions) {

    StringBuilder b = new StringBuilder();

    while (!regions.isEmpty()) {

      Layer prevLayer = null;
      Region prevRegion = null;

      for (SortedMap<Layer, Region> go : regions) {

        if (go.isEmpty())
          continue;

        Layer nextLayer = go.firstKey();
        Region nextRegion = go.get(nextLayer);

        if (prevLayer == null) {
          prevLayer = nextLayer;
          prevRegion = nextRegion;
          if (b.length() > 0)
            b.append(", ");
          b.append(nextRegion.getName());
        }

        if (prevLayer.equals(nextLayer) && prevRegion.equals(nextRegion)) {
          go.remove(prevLayer);
        } else {
          break;
        }
      }

      if (prevLayer == null)
        break;
    }

    return b.toString();
  }

  private static class LayerAndRegionComparator implements
      Comparator<SortedMap<Layer, Region>> {

    public int compare(SortedMap<Layer, Region> o1, SortedMap<Layer, Region> o2) {

      SortedSet<Layer> layers = new TreeSet<Layer>();
      layers.addAll(o1.keySet());
      layers.addAll(o2.keySet());

      for (Layer layer : layers) {

        Region r1 = o1.get(layer);
        Region r2 = o2.get(layer);

        if (r1 == null && r2 == null)
          throw new IllegalStateException();
        else if (r1 == null)
          return -1;
        else if (r2 == null)
          return 1;

        int rc = r1.compareTo(r2);

        if (rc != 0)
          return rc;
      }

      return 0;
    }
  }
}
