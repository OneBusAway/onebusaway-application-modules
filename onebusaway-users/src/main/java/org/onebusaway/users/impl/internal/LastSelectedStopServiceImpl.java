package org.onebusaway.users.impl.internal;

import java.util.Collections;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.onebusaway.users.services.internal.LastSelectedStopService;

public class LastSelectedStopServiceImpl implements LastSelectedStopService {

  private Cache _cache;

  public void setCache(Cache cache) {
    _cache = cache;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<String> getLastSelectedStopsForUser(Integer userId) {
    Element element = _cache.get(userId);
    if( element == null)
      return Collections.emptyList();
    return (List<String>) element.getValue();
  }

  @Override
  public void setLastSelectedStopsForUser(Integer userId, List<String> stopIds) {
    Element element = new Element(userId, stopIds);
    _cache.put(element);
  }
}
