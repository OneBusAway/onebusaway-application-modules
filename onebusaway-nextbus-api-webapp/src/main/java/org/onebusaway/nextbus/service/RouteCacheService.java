package org.onebusaway.nextbus.service;

import java.util.concurrent.ConcurrentHashMap;

public interface RouteCacheService {
  ConcurrentHashMap<String, String> getRouteShortNameToRouteIdMap();
  String getRouteShortNameFromId(String id);
}
