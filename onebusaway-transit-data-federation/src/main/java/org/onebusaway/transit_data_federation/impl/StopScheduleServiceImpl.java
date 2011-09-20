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
package org.onebusaway.transit_data_federation.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data_federation.model.ServiceDateSummary;
import org.onebusaway.transit_data_federation.services.ExtendedCalendarService;
import org.onebusaway.transit_data_federation.services.StopScheduleService;
import org.onebusaway.transit_data_federation.services.beans.AgencyBeanService;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndexService;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyBlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class StopScheduleServiceImpl implements StopScheduleService {

  private TransitGraphDao _transitGraphDao;

  private BlockIndexService _blockIndexService;

  private ExtendedCalendarService _calendarService;

  private AgencyBeanService _agencyBeanService;

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  @Autowired
  public void setBlockIndexService(BlockIndexService blockIndexService) {
    _blockIndexService = blockIndexService;
  }

  @Autowired
  public void setExtendedCalendarService(ExtendedCalendarService calendarService) {
    _calendarService = calendarService;
  }

  @Autowired
  public void setAgencyBeanService(AgencyBeanService agencyBeanService) {
    _agencyBeanService = agencyBeanService;
  }

  @Override
  public List<ServiceDateSummary> getServiceDateSummariesForStop(
      AgencyAndId stopId, boolean includePrivateService) {

    StopEntry stop = _transitGraphDao.getStopEntryForId(stopId, true);

    Set<ServiceIdActivation> allServiceIds = getAllServiceIdsForStop(stop,
        includePrivateService);

    Map<ServiceDate, Set<ServiceIdActivation>> serviceIdsByDate = new FactoryMap<ServiceDate, Set<ServiceIdActivation>>(
        new HashSet<ServiceIdActivation>());

    for (ServiceIdActivation serviceIds : allServiceIds) {
      Set<ServiceDate> serviceDates = _calendarService.getServiceDatesForServiceIds(serviceIds);
      for (ServiceDate serviceDate : serviceDates)
        serviceIdsByDate.get(serviceDate).add(serviceIds);
    }

    Map<Set<ServiceIdActivation>, List<ServiceDate>> datesByServiceIds = new FactoryMap<Set<ServiceIdActivation>, List<ServiceDate>>(
        new ArrayList<ServiceDate>());

    for (Map.Entry<ServiceDate, Set<ServiceIdActivation>> entry : serviceIdsByDate.entrySet()) {
      ServiceDate serviceDate = entry.getKey();
      Set<ServiceIdActivation> serviceIds = entry.getValue();
      datesByServiceIds.get(serviceIds).add(serviceDate);
    }

    List<ServiceDateSummary> summaries = new ArrayList<ServiceDateSummary>();

    for (Map.Entry<Set<ServiceIdActivation>, List<ServiceDate>> entry : datesByServiceIds.entrySet()) {
      Set<ServiceIdActivation> serviceIds = entry.getKey();
      List<ServiceDate> serviceDates = entry.getValue();
      Collections.sort(serviceDates);
      summaries.add(new ServiceDateSummary(serviceIds, serviceDates));
    }

    Collections.sort(summaries);

    return summaries;
  }

  private Set<ServiceIdActivation> getAllServiceIdsForStop(StopEntry stop,
      boolean includePrivateService) {

    Set<ServiceIdActivation> allServiceIds = new HashSet<ServiceIdActivation>();

    List<BlockStopTimeIndex> indices = _blockIndexService.getStopTimeIndicesForStop(stop);
    for (BlockStopTimeIndex index : indices)
      allServiceIds.add(index.getServiceIds());

    List<FrequencyBlockStopTimeIndex> frequencyIndices = _blockIndexService.getFrequencyStopTimeIndicesForStop(stop);
    for (FrequencyBlockStopTimeIndex index : frequencyIndices)
      allServiceIds.add(index.getServiceIds());

    for (Iterator<ServiceIdActivation> it = allServiceIds.iterator(); it.hasNext();) {
      ServiceIdActivation activation = it.next();
      LocalizedServiceId lsid = activation.getActiveServiceIds().get(0);
      String agencyId = lsid.getId().getAgencyId();
      AgencyBean bean = _agencyBeanService.getAgencyForId(agencyId);
      if (bean.isPrivateService() && !includePrivateService)
        it.remove();
    }

    return allServiceIds;
  }
}
