package org.onebusaway.tripplanner.impl;

import edu.washington.cs.rse.collections.tuple.Pair;
import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.onebusaway.gtdf.model.Stop;
import org.onebusaway.tripplanner.NoPathException;
import org.onebusaway.tripplanner.StopTransferWalkPlannerService;
import org.onebusaway.tripplanner.WalkPlannerService;
import org.onebusaway.tripplanner.model.Walk;

public class StopTransferWalkPlannerServiceImpl implements
    StopTransferWalkPlannerService {

  private WalkPlannerService _walkPlannerService;

  private Cache _cache;

  public void setWalkPlannerService(WalkPlannerService service) {
    _walkPlannerService = service;
  }

  public void setCache(Cache cache) {
    _cache = cache;
  }

  public Walk getWalkPlan(Stop from, Stop to) throws NoPathException {

    if (true) {
      CoordinatePoint latLonFrom = new CoordinatePoint(from.getLat(),
          from.getLon());
      CoordinatePoint latLonTo = new CoordinatePoint(to.getLat(), to.getLon());
      return _walkPlannerService.getWalkPlan(latLonFrom, from.getLocation(),
          latLonTo, to.getLocation());
    }

    String idFrom = from.getId();
    String idTo = to.getId();

    Pair<String> key = Pair.createPair(idFrom, idTo);

    if (idFrom.compareTo(idTo) > 0)
      key = key.swap();

    Element element = _cache.get(key);

    if (element == null) {
      Walk walk = null;
      try {
        walk = _walkPlannerService.getWalkPlan(from.getLocation(),
            to.getLocation());
      } catch (NoPathException ex) {

      }
      element = new Element(key, walk);
      _cache.put(element);
    }

    if (element.getValue() == null)
      throw new NoPathException();

    return (Walk) element.getValue();
  }

}
