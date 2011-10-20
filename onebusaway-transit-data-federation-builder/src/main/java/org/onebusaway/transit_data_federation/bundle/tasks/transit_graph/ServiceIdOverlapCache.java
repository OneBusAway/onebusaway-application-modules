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
package org.onebusaway.transit_data_federation.bundle.tasks.transit_graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServiceIdOverlapCache {

  private CalendarService _calendarService;

  private Map<Set<LocalizedServiceId>, List<ServiceIdActivation>> _cache = new HashMap<Set<LocalizedServiceId>, List<ServiceIdActivation>>();

  @Autowired
  public void setCalendarService(CalendarService calendarService) {
    _calendarService = calendarService;
  }

  public List<ServiceIdActivation> getOverlappingServiceIdCombinations(
      Set<LocalizedServiceId> serviceIds) {

    List<ServiceIdActivation> combinations = _cache.get(serviceIds);
    if (combinations == null) {
      combinations = computeCombinationsInternal(serviceIds);
      _cache.put(new HashSet<LocalizedServiceId>(serviceIds), combinations);
    }

    return combinations;
  }

  private List<ServiceIdActivation> computeCombinationsInternal(
      Set<LocalizedServiceId> serviceIds) {

    Map<ServiceDate, Set<LocalizedServiceId>> serviceIdsByServiceDate = new FactoryMap<ServiceDate, Set<LocalizedServiceId>>(
        new HashSet<LocalizedServiceId>());

    for (LocalizedServiceId lsid : serviceIds) {
      AgencyAndId serviceId = lsid.getId();
      for (ServiceDate serviceDate : _calendarService.getServiceDatesForServiceId(serviceId))
        serviceIdsByServiceDate.get(serviceDate).add(lsid);
    }

    Set<Set<LocalizedServiceId>> sets = new HashSet<Set<LocalizedServiceId>>();
    sets.addAll(serviceIdsByServiceDate.values());

    List<ServiceIdActivation> combinations = new ArrayList<ServiceIdActivation>();

    for (Set<LocalizedServiceId> activeServiceIds : sets) {

      Set<LocalizedServiceId> inactiveServiceIds = new HashSet<LocalizedServiceId>();
      for (Set<LocalizedServiceId> combo2 : sets) {
        if (isSubset(activeServiceIds, combo2))
          inactiveServiceIds.addAll(combo2);
      }
      inactiveServiceIds.removeAll(activeServiceIds);
      
      combinations.add(new ServiceIdActivation(list(activeServiceIds),
          list(inactiveServiceIds)));
    }

    Collections.sort(combinations);

    return combinations;
  }

  private <T> boolean isSubset(Set<T> potentialSubset, Set<T> potentialSuperset) {
    if (potentialSubset.size() >= potentialSuperset.size())
      return false;
    for (T element : potentialSubset) {
      if (!potentialSuperset.contains(element))
        return false;
    }
    return true;
  }

  private List<LocalizedServiceId> list(
      Collection<LocalizedServiceId> combination) {
    List<LocalizedServiceId> serviceIds = new ArrayList<LocalizedServiceId>(
        combination);
    Collections.sort(serviceIds);
    return serviceIds;
  }
}
