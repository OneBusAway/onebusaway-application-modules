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
package org.onebusaway.admin.service.bundle.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.onebusaway.admin.service.bundle.BundleBuildResponseDao;
import org.onebusaway.admin.service.bundle.GtfsArchiveDao;
import org.onebusaway.admin.service.bundle.GtfsArchiveService;
import org.onebusaway.admin.service.bundle.task.model.ArchivedAgency;
import org.onebusaway.admin.service.bundle.task.model.ArchivedCalendar;
import org.onebusaway.admin.service.bundle.task.model.ArchivedRoute;
import org.onebusaway.admin.service.bundle.task.model.ArchivedStopTime;
import org.onebusaway.admin.service.bundle.task.model.ArchivedTrip;
import org.onebusaway.admin.service.bundle.task.model.GtfsBundleInfo;
import org.springframework.beans.factory.annotation.Autowired;

public class GtfsArchiveServiceImpl implements GtfsArchiveService {

  @Autowired
  private GtfsArchiveDao _gtfsArchiveDao;


  @Override
  public List<ArchivedAgency> getAllAgenciesByBundleId(int buildId) {
    List<ArchivedAgency> agencies = new ArrayList<>();
    agencies = _gtfsArchiveDao.getAllAgenciesByBundleId(buildId);

    return agencies;
  }

  @Override
  public List<ArchivedCalendar> getAllCalendarsByBundleId(int buildId) {
    List<ArchivedCalendar> calendars = new ArrayList<>();
    calendars = _gtfsArchiveDao.getAllCalendarsByBundleId(buildId);

    return calendars;
  }

  @Override
  public SortedSet<String> getAllDatasets() {
    SortedSet<String> datasets = new TreeSet<>();
    datasets = _gtfsArchiveDao.getAllDatasets();

    return datasets;
  }

  @Override
  public List<ArchivedRoute> getAllRoutesByBundleId(int buildId) {
    List<ArchivedRoute> routes = new ArrayList<>();
    routes = _gtfsArchiveDao.getAllRoutesByBundleId(buildId);

    return routes;
  }

  @Override
  public List<ArchivedTrip> getAllTripsByBundleId(int buildId) {
    List<ArchivedTrip> trips = new ArrayList<>();
    trips = _gtfsArchiveDao.getAllTripsByBundleId(buildId);

    return trips;
  }

  @Override
  public SortedSet<String> getBuildNamesForDataset(String dataset) {
    SortedSet<String> buildNames = new TreeSet<>();
    buildNames = _gtfsArchiveDao.getBuildNamesForDataset(dataset);

    return buildNames;
  }
  
  @Override
  public SortedMap<String, String> getBuildNameMapForDataset(String dataset) {
    SortedMap<String, String> buildNameMap = new TreeMap<>();
    buildNameMap = _gtfsArchiveDao.getBuildNameMapForDataset(dataset);

    return buildNameMap;
  }

  @Override
  public Date getBundleStartDate(int buildId) {
    GtfsBundleInfo bundleInfo = _gtfsArchiveDao.getBundleInfoForId(buildId);
    Date startDate = bundleInfo == null ? null : bundleInfo.getStartDate();
    return startDate;
  }

  @Override
  public List<ArchivedRoute> getRoutesForAgencyAndBundleId(
      String agencyId, int buildId) {
    List<ArchivedRoute> routes = new ArrayList<>();
    routes = _gtfsArchiveDao.getRoutesForAgencyAndBundleId(agencyId, buildId);

    return routes;
  }

  @Override
  public List<ArchivedStopTime> getStopTimesForTripAndBundleId(
      ArchivedTrip trip, int buildId) {
    List<ArchivedStopTime> stopTimes = new ArrayList<>();
    stopTimes = _gtfsArchiveDao.getStopTimesForTripAndBundleId(trip, buildId);

    return stopTimes;
  }

  @Override
  public List getTripsForRouteAndBundleId(String routeId,
      int buildId) {
    List<ArchivedTrip> trips = new ArrayList<>();
    trips = _gtfsArchiveDao.getTripsForRouteAndBundleId(routeId, buildId);

    return trips;
  }

  @Override
  public List<Object[]> getTripStopCounts(int buildId) {
    List<Object[]>results = null;
    results = _gtfsArchiveDao.getTripStopCounts(buildId);

    return results;
  }

}
