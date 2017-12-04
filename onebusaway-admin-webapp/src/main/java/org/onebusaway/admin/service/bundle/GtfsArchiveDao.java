/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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
package org.onebusaway.admin.service.bundle;

import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;

import org.onebusaway.admin.service.bundle.task.model.ArchivedAgency;
import org.onebusaway.admin.service.bundle.task.model.ArchivedCalendar;
import org.onebusaway.admin.service.bundle.task.model.ArchivedRoute;
import org.onebusaway.admin.service.bundle.task.model.ArchivedStopTime;
import org.onebusaway.admin.service.bundle.task.model.ArchivedTrip;
import org.onebusaway.admin.service.bundle.task.model.GtfsBundleInfo;

/**
 * This dao has been added to support reporting against archived bundle data.
 * 
 * @author jpearson
 *
 */
public interface GtfsArchiveDao {
  List<ArchivedAgency> getAllAgenciesByBundleId(int buildId);
  
  List<ArchivedCalendar> getAllCalendarsByBundleId(int buildId);

  SortedSet<String> getAllDatasets();

  List<ArchivedRoute> getAllRoutesByBundleId(int buildId);

  List<ArchivedTrip> getAllTripsByBundleId(int buildId);

  SortedSet<String> getBuildNamesForDataset(String dataset);

  SortedMap<String, String> getBuildNameMapForDataset(String dataset);

  GtfsBundleInfo getBundleInfoForId(int buildId);

  List<ArchivedRoute> getRoutesForAgencyAndBundleId(
      String agencyId, int buildId);

  List<ArchivedStopTime> getStopTimesForTripAndBundleId(
      ArchivedTrip trip, int buildId);

  List<ArchivedTrip> getTripsForRouteAndBundleId(String routeId,
      int buildId);

  List getTripStopCounts(int buildId);
}
