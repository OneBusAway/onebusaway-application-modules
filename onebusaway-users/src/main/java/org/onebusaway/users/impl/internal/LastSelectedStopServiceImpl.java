/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.users.impl.internal;

import java.util.ArrayList;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.onebusaway.users.services.internal.LastSelectedStopService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LastSelectedStopServiceImpl implements LastSelectedStopService {
  
  private static Logger _log = LoggerFactory.getLogger(LastSelectedStopServiceImpl.class);

  private Cache _cache;

  public void setCache(Cache cache) {
    _cache = cache;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<String> getLastSelectedStopsForUser(Integer userId) {
    Element element = _cache.get(userId);
    if( element == null) {
      if( _log.isDebugEnabled() )
        _log.debug("getting: userId=" + userId + " stopIds=none");
      return new ArrayList<String>();
    }
    if( _log.isDebugEnabled() )
      _log.debug("getting: userId=" + userId + " stopIds=" + element.getValue());
    return (List<String>) element.getValue();
  }

  @Override
  public void setLastSelectedStopsForUser(Integer userId, List<String> stopIds) {
    Element element = new Element(userId, stopIds);
    if( _log.isDebugEnabled() )
      _log.debug("putting: userId=" + userId + " stopIds=" + stopIds);
    _cache.put(element);
  }

  @Override
  public void clearLastSelectedStopForUser(Integer userId) {
    _cache.remove(userId);
  }
}
