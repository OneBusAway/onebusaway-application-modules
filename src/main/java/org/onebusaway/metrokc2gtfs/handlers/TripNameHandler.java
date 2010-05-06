package org.onebusaway.metrokc2gtfs.handlers;

import com.vividsolutions.jts.geom.Point;

import org.onebusaway.common.model.Layer;
import org.onebusaway.common.model.Region;
import org.onebusaway.common.services.ProjectionService;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.metrokc2gtfs.TranslationContext;
import org.onebusaway.metrokc2gtfs.impl.LocationNamingStrategy;
import org.onebusaway.metrokc2gtfs.impl.MetroDao;
import org.onebusaway.metrokc2gtfs.model.MetroKCBlockTrip;
import org.onebusaway.metrokc2gtfs.model.MetroKCServicePattern;
import org.onebusaway.metrokc2gtfs.model.MetroKCTrip;

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

  public TripNameHandler(TranslationContext context) {
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
      for (MetroKCServicePattern pattern : servicePatterns)
        regionsByServicePattern.put(pattern, getRegions(pattern));
      pruneCommonLayers(regionsByServicePattern);
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

        MetroKCTrip trip = _dao.getTripById(bt.getTripId());
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
   * @param regionsByServicePattern
   */
  private void pruneCommonLayers(
      Map<MetroKCServicePattern, SortedMap<Layer, Region>> regionsByServicePattern) {

    while (true) {

      Layer currentLayer = null;
      Region currentRegion = null;

      for (SortedMap<Layer, Region> regions : regionsByServicePattern.values()) {

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
      // System.out.println("removing common region: " +
      // currentRegion.getName());

      for (SortedMap<Layer, Region> regions : regionsByServicePattern.values())
        regions.remove(currentLayer);
    }
  }

  private SortedMap<Layer, Region> getRegions(
      MetroKCServicePattern servicePattern) {

    MetroKCShapePoint last = _dao.getLastShapePointByServicePattern(servicePattern);
    Point location = _projection.getXYAsPoint(last.getX(), last.getY());
    return _locationNaming.getRegionsByLocation(location);
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
