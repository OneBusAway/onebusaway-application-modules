package org.onebusaway.kcmetro2gtfs.handlers;

import org.onebusaway.gtfs.model.Route;
import org.onebusaway.kcmetro2gtfs.TranslationContext;
import org.onebusaway.kcmetro2gtfs.impl.LocationNamingStrategy;
import org.onebusaway.kcmetro2gtfs.impl.MetroDao;
import org.onebusaway.kcmetro2gtfs.model.MetroKCBlockTrip;
import org.onebusaway.kcmetro2gtfs.model.MetroKCServicePattern;
import org.onebusaway.kcmetro2gtfs.model.MetroKCShapePoint;
import org.onebusaway.kcmetro2gtfs.model.MetroKCTrip;
import org.onebusaway.kcmetro2gtfs.services.ProjectionService;
import org.onebusaway.layers.model.Layer;
import org.onebusaway.layers.model.Region;

import com.vividsolutions.jts.geom.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class TripNameHandler implements Runnable {

  private MetroDao _dao;

  private ProjectionService _projection;

  private LocationNamingStrategy _locationNaming;

  private Map<MetroKCTrip, String> _tripToName = new HashMap<MetroKCTrip, String>();

  private TranslationContext _context;

  public TripNameHandler(TranslationContext context) {
    _context = context;
    _dao = context.getDao();
    _projection = context.getProjectionService();
    _locationNaming = context.getLocationNamingStrategy();

  }

  public String getNameByTrip(MetroKCTrip trip) {
    return _tripToName.get(trip);
  }

  public void run() {

    Map<MetroKCServicePattern, SortedMap<Layer, Region>> allRegionsByServicePattern = new HashMap<MetroKCServicePattern, SortedMap<Layer, Region>>();

    for (Route route : _dao.getAllRoutes()) {
      Set<MetroKCServicePattern> servicePatterns = _dao.getServicePatternsByRoute(route);
      Map<MetroKCServicePattern, SortedMap<Layer, Region>> regionsByServicePattern = new HashMap<MetroKCServicePattern, SortedMap<Layer, Region>>();
      for (MetroKCServicePattern pattern : servicePatterns) {
        SortedMap<Layer, Region> regions = getRegions(pattern);
        regionsByServicePattern.put(pattern, regions);
      }
      pruneCommonLayers(route, regionsByServicePattern);
      allRegionsByServicePattern.putAll(regionsByServicePattern);
    }

    Map<Integer, List<MetroKCBlockTrip>> blocksByBlockId = _dao.getAllBlockTripsByBlockId();

    int blocksIndex = 0;

    for (List<MetroKCBlockTrip> blocks : blocksByBlockId.values()) {

      if (blocksIndex % 1000 == 0)
        System.out.println("blocks=" + blocksIndex + "/"
            + blocksByBlockId.size());
      blocksIndex++;

      MetroKCServicePattern prevPattern = null;
      LinkedList<SortedMap<Layer, Region>> regionsWithContinuations = new LinkedList<SortedMap<Layer, Region>>();

      for (int i = blocks.size() - 1; i >= 0; i--) {

        MetroKCBlockTrip bt = blocks.get(i);

        MetroKCTrip trip = _dao.getTripById(bt.getFullTripId());
        MetroKCServicePattern p = _dao.getServicePatternByTrip(trip);

        SortedMap<Layer, Region> regions = allRegionsByServicePattern.get(p);

        if (!(prevPattern != null
            && prevPattern.getRoute().equals(p.getRoute()) && prevPattern.getDirection().equals(
            p.getDirection()))) {
          regionsWithContinuations.clear();
        }

        regionsWithContinuations.addFirst(regions);
        String name = getRegionsAsName(regionsWithContinuations);
        _tripToName.put(trip, name);

        prevPattern = p;
      }
    }

  }

  /**
   * Remove all common layer-regions for a set of service patterns.
   * 
   * @param route
   * @param regionsByServicePattern
   */
  private void pruneCommonLayers(
      Route route,
      Map<MetroKCServicePattern, SortedMap<Layer, Region>> regionsByServicePattern) {

    while (true) {

      Layer currentLayer = null;
      Region currentRegion = null;

      for (Map.Entry<MetroKCServicePattern, SortedMap<Layer, Region>> entry : regionsByServicePattern.entrySet()) {

        MetroKCServicePattern servicePattern = entry.getKey();
        SortedMap<Layer, Region> regions = entry.getValue();

        if (regions.isEmpty())
          throw new IllegalStateException("no regions for route: " + route
              + " servicePattern=" + servicePattern.getId());
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
      // System.out.println("removing common region: " +
      // currentRegion.getName());

      for (SortedMap<Layer, Region> regions : regionsByServicePattern.values())
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

  private String getRegionsAsName(
      List<SortedMap<Layer, Region>> consecutiveRegions) {

    StringBuilder b = new StringBuilder();

    // Make a copy of the incoming data structure, since we will be modifying it
    List<SortedMap<Layer, Region>> c = new ArrayList<SortedMap<Layer, Region>>(
        consecutiveRegions.size());
    for (SortedMap<Layer, Region> regions : consecutiveRegions)
      c.add(new TreeMap<Layer, Region>(regions));
    consecutiveRegions = c;

    while (true) {

      Layer prevLayer = null;
      Region prevRegion = null;

      for (SortedMap<Layer, Region> regions : consecutiveRegions) {

        if (regions.isEmpty())
          continue;

        Layer nextLayer = regions.firstKey();
        Region nextRegion = regions.get(nextLayer);

        if (prevLayer == null) {
          prevLayer = nextLayer;
          prevRegion = nextRegion;
          if (b.length() > 0)
            b.append(", ");
          b.append(nextRegion.getName());
        }

        if (prevLayer.equals(nextLayer) && prevRegion.equals(nextRegion)) {
          regions.remove(prevLayer);
        } else {
          break;
        }
      }

      if (prevLayer == null)
        break;
    }

    return b.toString();
  }
}
