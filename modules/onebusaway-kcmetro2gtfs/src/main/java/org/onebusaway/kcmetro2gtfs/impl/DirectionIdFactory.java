package org.onebusaway.kcmetro2gtfs.impl;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.kcmetro2gtfs.model.MetroKCTrip;

import edu.washington.cs.rse.collections.FactoryMap;

import java.util.HashMap;
import java.util.Map;

public class DirectionIdFactory {

  private Map<AgencyAndId, Map<String, String>> _directionIdsByRouteAndDirectionName = new FactoryMap<AgencyAndId, Map<String, String>>(
      new HashMap<String, String>());

  public String getDirectionId(AgencyAndId routeId, MetroKCTrip trip) {
    Map<String, String> ids = _directionIdsByRouteAndDirectionName.get(routeId);
    String directionName = trip.getDirectionName();
    String directionCode = ids.get(directionName);
    if (directionCode == null) {
      directionCode = Integer.toString(ids.size());
      ids.put(directionName, directionCode);
    }
    return directionCode;
  }
}
