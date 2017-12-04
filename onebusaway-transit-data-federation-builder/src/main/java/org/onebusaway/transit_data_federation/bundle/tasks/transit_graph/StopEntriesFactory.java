/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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
import java.util.Map;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data.model.EAccessibility;
import org.onebusaway.transit_data_federation.impl.transit_graph.AgencyEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TransitGraphImpl;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.util.LoggingIntervalUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StopEntriesFactory {

  private Logger _log = LoggerFactory.getLogger(StopEntriesFactory.class);

  private GtfsRelationalDao _gtfsDao;

  @Autowired
  public void setGtfsDao(GtfsRelationalDao gtfsDao) {
    _gtfsDao = gtfsDao;
  }

  /**
   * Iterate over each stop, generating a StopEntry for the graph.
   * 
   * @param graph
   */
  public void processStops(TransitGraphImpl graph) {

    int stopIndex = 0;

    Collection<Stop> stops = _gtfsDao.getAllStops();
    int logInterval = LoggingIntervalUtil.getAppropriateLoggingInterval(stops.size());
    
    Map<String, ArrayList<StopEntry>> stopEntriesByAgencyId = new FactoryMap<String, ArrayList<StopEntry>>(
        new ArrayList<StopEntry>());

    for (Stop stop : stops) {

      if (stopIndex % logInterval == 0)
        _log.info("stops: " + stopIndex + "/" + stops.size());
      stopIndex++;

      StopEntryImpl stopEntry = new StopEntryImpl(stop.getId(), stop.getLat(),
          stop.getLon());
      stopEntry.setWheelchairBoarding(getWheelchairBoardingAccessibilityForStop(stop));
      graph.putStopEntry(stopEntry);
      stopEntriesByAgencyId.get(stop.getId().getAgencyId()).add(stopEntry);
    }

    for (Map.Entry<String, ArrayList<StopEntry>> entry : stopEntriesByAgencyId.entrySet()) {
      String agencyId = entry.getKey();
      ArrayList<StopEntry> stopEntries = entry.getValue();
      stopEntries.trimToSize();
      AgencyEntryImpl agency = graph.getAgencyForId(agencyId);
      if (agency == null) {
        String msg = null;
        if (stopEntries.size() > 0) {
          msg = "no agency found for agencyId=" + agencyId + " of stop entry " + stopEntries.get(0).getId();
        } else {
        msg = "no agency found for agencyId=" + agencyId + " of empty stop entry list.";
        }
        _log.error(msg);
        throw new IllegalStateException(msg);
      }
      agency.setStops(stopEntries);
    }

    graph.refreshStopMapping();
  }

  private EAccessibility getWheelchairBoardingAccessibilityForStop(Stop stop) {
    switch (stop.getWheelchairBoarding()) {
      case 1:
        return EAccessibility.ACCESSIBLE;
      case 2:
        return EAccessibility.NOT_ACCESSIBLE;
      default:
        return EAccessibility.UNKNOWN;
    }
  }
}
