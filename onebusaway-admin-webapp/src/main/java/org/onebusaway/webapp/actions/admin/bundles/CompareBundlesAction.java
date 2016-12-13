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
package org.onebusaway.webapp.actions.admin.bundles;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.onebusaway.admin.model.ui.DataValidationDirectionCts;
import org.onebusaway.admin.model.ui.DataValidationHeadsignCts;
import org.onebusaway.admin.model.ui.DataValidationMode;
import org.onebusaway.admin.model.ui.DataValidationRouteCounts;
import org.onebusaway.admin.model.ui.DataValidationStopCt;
import org.onebusaway.admin.service.DiffService;
import org.onebusaway.admin.service.FileService;
import org.onebusaway.admin.service.FixedRouteParserService;
import org.onebusaway.admin.service.bundle.GtfsArchiveService;
import org.onebusaway.admin.service.bundle.task.model.ArchivedAgency;
import org.onebusaway.admin.service.bundle.task.model.ArchivedCalendar;
import org.onebusaway.admin.service.bundle.task.model.ArchivedRoute;
import org.onebusaway.admin.service.bundle.task.model.ArchivedStopTime;
import org.onebusaway.admin.service.bundle.task.model.ArchivedTrip;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.util.services.configuration.ConfigurationServiceClient;
import org.onebusaway.webapp.actions.OneBusAwayNYCAdminActionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Action class used by Transit Data Bundle Utility to compare two bundles.
 *
 */
@Namespace(value="/admin/bundles")
@Results({
  @Result(name="diffResult", type="json", 
  params={"root", "combinedDiffs"})
})
public class CompareBundlesAction extends OneBusAwayNYCAdminActionSupport {
  private static Logger _log = LoggerFactory.getLogger(CompareBundlesAction.class);
  private static final long serialVersionUID = 1L;
  private static final char ID_SEPARATOR = '_';
  private static final int MAX_STOP_CT = 200;

  private boolean useArchived;
  private String datasetName;
  private int dataset_1_build_id;
  private String buildName;
  private String datasetName2;
  private int dataset_2_build_id;
  private String buildName2;
  private FileService fileService;
  private GtfsArchiveService _gtfsArchiveService;
  private List<String> diffResult = new ArrayList<String>();
  private Map<String, List> combinedDiffs;
  private DiffService diffService;
  @Autowired
  private FixedRouteParserService _fixedRouteParserService;

  public boolean isUseArchived() {
    return useArchived;
  }

  public void setUseArchived(boolean useArchived) {
    this.useArchived = useArchived;
  }

  public void setDatasetName(String datasetName) {
    this.datasetName = datasetName;
  }

  public void setDataset_1_build_id(int dataset_1_build_id) {
    this.dataset_1_build_id = dataset_1_build_id;
  }

  public void setBuildName(String buildName) {
    this.buildName = buildName;
  }

  public void setDatasetName2(String datasetName2) {
    this.datasetName2 = datasetName2;
  }

  public void setDataset_2_build_id(int dataset_2_build_id) {
    this.dataset_2_build_id = dataset_2_build_id;
  }

  public void setBuildName2(String buildName2) {
    this.buildName2 = buildName2;
  }

  @Autowired
  public void setFileService(FileService fileService) {
    this.fileService = fileService;
  }

  @Autowired
  public void setGtfsArchiveService(GtfsArchiveService gtfsArchiveService) {
    this._gtfsArchiveService = gtfsArchiveService;
  }

  public Map<String, List> getCombinedDiffs() {
    return combinedDiffs;
  }

  @Autowired
  private ConfigurationServiceClient _configurationServiceClient;

  @Autowired
  public void setDiffService(DiffService diffService) {
    this.diffService = diffService;
  }

  public String diffResult() {
    String gtfsStatsFile1 = fileService.getBucketName() + File.separator 
        + datasetName + "/builds/" + buildName + "/outputs/gtfs_stats.csv"; 
    String gtfsStatsFile2 = fileService.getBucketName()
        + File.separator
        + datasetName2 + "/builds/"
        + buildName2 + "/outputs/gtfs_stats.csv";
    diffResult.clear();
    diffResult = diffService.diff(gtfsStatsFile2, gtfsStatsFile1);

    // Added code to compare Fixed Route Date Validation reports from the
    // two specified bundles and builds
    List<DataValidationMode> fixedRouteDiffs = compareFixedRouteValidations(
        datasetName, buildName, datasetName2, buildName2);
    combinedDiffs = new HashMap<String, List>();
    combinedDiffs.put("diffResults", diffResult);
    combinedDiffs.put("fixedRouteDiffs", fixedRouteDiffs);
    return "diffResult";
  }

  private List<DataValidationMode> compareFixedRouteValidations(
      String datasetName, String buildName,
      String datasetName2, String buildName2) {
    List<DataValidationMode> currentModes;
    List<DataValidationMode> selectedModes;
    if (!useArchived) {
      String currentValidationReportPath = fileService.getBucketName()
          + File.separator  + datasetName + "/builds/" + buildName
          + "/outputs/fixed_route_validation.csv";
      File currentValidationReportFile = new File(currentValidationReportPath);
      String selectedValidationReportPath = fileService.getBucketName()
          + File.separator
          + datasetName2 + "/builds/"
          + buildName2 + "/outputs/fixed_route_validation.csv";
      File selectedValidationReportFile = new File(selectedValidationReportPath);

      // parse input files
      currentModes
        = _fixedRouteParserService.parseFixedRouteReportFile(currentValidationReportFile);
      selectedModes
        = _fixedRouteParserService.parseFixedRouteReportFile(selectedValidationReportFile);
    } else {
      currentModes = buildModes(dataset_1_build_id);
      selectedModes = buildModes(dataset_2_build_id);
    }

    // compare and get diffs
    List<DataValidationMode> fixedRouteDiffs
      = findFixedRouteDiffs(currentModes, selectedModes);

    return  fixedRouteDiffs;
  }

  private List<DataValidationMode> buildModes(int buildId) {
    List<DataValidationMode> modes = new ArrayList<>();

    // Get bundle start date
    Date bundleStartDate = _gtfsArchiveService.getBundleStartDate(buildId);
    LocalDate start = new LocalDate(bundleStartDate.getTime());

    // Get dates for checking trips for days of the week
    LocalDate firstMon = getFirstDay(DateTimeConstants.MONDAY, start);
    LocalDate firstTues = getFirstDay(DateTimeConstants.TUESDAY, start);
    LocalDate firstWed = getFirstDay(DateTimeConstants.WEDNESDAY, start);
    LocalDate firstThur = getFirstDay(DateTimeConstants.THURSDAY, start);
    LocalDate firstFri = getFirstDay(DateTimeConstants.FRIDAY, start);
    LocalDate firstSat = getFirstDay(DateTimeConstants.SATURDAY, start);
    LocalDate firstSun = getFirstDay(DateTimeConstants.SUNDAY, start);

    // Get the service ids for weekdays, Saturdays, and Sundays
    Set<AgencyAndId> weekdaySvcIds = new HashSet<>();
    Set<AgencyAndId> saturdaySvcIds = new HashSet<>();
    Set<AgencyAndId> sundaySvcIds = new HashSet<>();

    // Check service ids
    List<ArchivedCalendar> calendars =
        _gtfsArchiveService.getAllCalendarsByBundleId(buildId);
    for (ArchivedCalendar calendar : calendars) {
      Date svcStartDate = calendar.getStartDate().getAsDate();
      LocalDate jodaStartDate = new LocalDate(svcStartDate);
      Date svcEndDate = calendar.getEndDate().getAsDate();
      LocalDate jodaEndDate = new LocalDate(svcEndDate);
      if (calendar.getMonday() == 1 && !firstMon.isBefore(jodaStartDate)
          && !firstMon.isAfter(jodaEndDate)) {
        weekdaySvcIds.add(calendar.getServiceId());
      }
      if (calendar.getTuesday() == 1 && !firstTues.isBefore(jodaStartDate)
          && !firstTues.isAfter(jodaEndDate)) {
        weekdaySvcIds.add(calendar.getServiceId());
      }
      if (calendar.getWednesday() == 1 && !firstWed.isBefore(jodaStartDate)
          && !firstWed.isAfter(jodaEndDate)) {
        weekdaySvcIds.add(calendar.getServiceId());
      }
      if (calendar.getThursday() == 1 && !firstThur.isBefore(jodaStartDate)
          && !firstThur.isAfter(jodaEndDate)) {
        weekdaySvcIds.add(calendar.getServiceId());
      }
      if (calendar.getFriday() == 1 && !firstFri.isBefore(jodaStartDate)
          && !firstFri.isAfter(jodaEndDate)) {
        weekdaySvcIds.add(calendar.getServiceId());
      }
      if (calendar.getSaturday() == 1 && !firstSat.isBefore(jodaStartDate)
          && !firstSat.isAfter(jodaEndDate)) {
        saturdaySvcIds.add(calendar.getServiceId());
      }
      if (calendar.getSunday() == 1 && !firstSun.isBefore(jodaStartDate)
          && !firstSun.isAfter(jodaEndDate)) {
        sundaySvcIds.add(calendar.getServiceId());
      }
    }

    Map<String, List<String>> reportModes = getReportModes();
    Collection<ArchivedAgency> agencies = _gtfsArchiveService.getAllAgenciesByBundleId(buildId);
    for (String currentMode : reportModes.keySet()) {
      DataValidationMode newMode = new DataValidationMode();
      newMode.setModeName(currentMode);
      SortedSet<DataValidationRouteCounts> newModeRouteCts = new TreeSet<DataValidationRouteCounts>();
      List<String> agenciesOrRoutes = reportModes.get(currentMode);  // Note: currentRoutes entries might be just an agency id
      for (String agencyOrRoute : agenciesOrRoutes) {                // or a route, i.e. <agencyId>_<routeId>
        List<ArchivedRoute> routes = null;
        int idx = agencyOrRoute.indexOf("_"); // check if agency or route id
        if (idx < 0) {    // it's an agency
          routes = _gtfsArchiveService.getRoutesForAgencyAndBundleId(agencyOrRoute, buildId);
        } else {
          ;
        }
        if (routes == null || routes.size() ==  0) {
          continue;
        }
        int routeCt = routes.size();
        int currentRouteCt=0;
        for (ArchivedRoute route : routes) {
          currentRouteCt++;
          int[] wkdayTrips = null;
          int[] satTrips = null;
          int[] sunTrips = null;
          Map<String, TripTotals> tripMap = new HashMap<>();
          String routeId = route.getAgencyId() + ID_SEPARATOR + route.getId();
          DataValidationRouteCounts newRouteCts = new DataValidationRouteCounts();
          //String routeName = route.getShortName() + "-" + route.getDesc();
          String routeName = route.getDesc();
          if (routeName == null || routeName.equals("null") || routeName.isEmpty()) {
            routeName = route.getLongName();
          }
          if (routeName == null || routeName.equals("null")) {
            routeName =  "";
          }
          newRouteCts.setRouteName(routeName);
          String routeNum = route.getShortName();
          if (routeNum == null || routeNum.equals("null") || routeNum.isEmpty()) {
            routeNum = route.getId();
          }
          if (routeNum == null || routeNum.equals("null")) {
            routeNum =  "";
          }
          newRouteCts.setRouteNum(routeNum);
          SortedSet<DataValidationHeadsignCts> headsignCounts = new TreeSet<>();
          List<ArchivedTrip> trips = _gtfsArchiveService.getTripsForRouteAndBundleId(routeId, buildId);
          for (ArchivedTrip trip : trips) {
            List<ArchivedStopTime> stopTimes =  _gtfsArchiveService.getStopTimesForTripAndBundleId(trip, buildId);
            int stopCt = stopTimes.size();
            if (stopCt > MAX_STOP_CT) {
              stopCt = MAX_STOP_CT;
            }
            TripTotals tripTotals = null;
            String tripHeadsign = trip.getTripHeadsign();
            tripHeadsign = tripHeadsign == null ? "" : tripHeadsign;
            if (tripMap.containsKey(tripHeadsign)) {
              tripTotals = tripMap.get(tripHeadsign);
            } else {
              tripTotals = new TripTotals();
              tripMap.put(tripHeadsign, tripTotals);
            }
            /*
             * TODO: if stopCt exceeds array sizes, resize arrays
             */
            if (trip.getDirectionId() == null || trip.getDirectionId().equals("0")) {
              wkdayTrips = tripTotals.wkdayTrips_0;
              satTrips = tripTotals.satTrips_0;
              sunTrips = tripTotals.sunTrips_0;
            } else {
              wkdayTrips = tripTotals.wkdayTrips_1;
              satTrips = tripTotals.satTrips_1;
              sunTrips = tripTotals.sunTrips_1;
            }
            //AgencyAndId tripSvcId = trip.getServiceId();
            AgencyAndId tripSvcId = new AgencyAndId(trip.getServiceId_agencyId(), trip.getServiceId_id());
            if (weekdaySvcIds.contains(tripSvcId)) {
              ++wkdayTrips[stopCt];
            } else if (saturdaySvcIds.contains(tripSvcId)) {
              ++satTrips[stopCt];
            } else if (sundaySvcIds.contains(tripSvcId)) {
              ++sunTrips[stopCt];
            }
            tripMap.put(tripHeadsign, tripTotals);
          }  // End of trips loop.  Stop counts by direction for this route have been set.
          for (String headSign : tripMap.keySet() ) {
            TripTotals tripTotals = tripMap.get(headSign);
            DataValidationHeadsignCts newHeadsignCt = new DataValidationHeadsignCts();
            newHeadsignCt.setHeadsign(headSign);
            SortedSet<DataValidationDirectionCts> newDirCountSet = new TreeSet<DataValidationDirectionCts>();

            DataValidationDirectionCts newDirCt_0 = new DataValidationDirectionCts();
            newDirCt_0.setDirection("0");
            SortedSet<DataValidationStopCt> stopCounts_0 = new TreeSet<>();
            for (int i=0; i<MAX_STOP_CT; ++i) {
              if (tripTotals.wkdayTrips_0[i]>0
                  || tripTotals.satTrips_0[i]>0
                  || tripTotals.sunTrips_0[i]>0) {
                DataValidationStopCt stopCt_0 = new DataValidationStopCt();
                stopCt_0.setStopCt(i);
                stopCt_0.setTripCts(new int[]
                    {tripTotals.wkdayTrips_0[i], tripTotals.satTrips_0[i], tripTotals.sunTrips_0[i]});
                stopCounts_0.add(stopCt_0);
              }
            }
            if (stopCounts_0.size() > 0) {
              newDirCt_0.setStopCounts(stopCounts_0);
              newDirCountSet.add(newDirCt_0);
            }
            DataValidationDirectionCts newDirCt_1 = new DataValidationDirectionCts();
            newDirCt_1.setDirection("1");
            SortedSet<DataValidationStopCt> stopCounts_1 = new TreeSet<>();
            for (int i=0; i<MAX_STOP_CT; ++i) {
              if (tripTotals.wkdayTrips_1[i]>0
                  || tripTotals.satTrips_1[i]>0
                  || tripTotals.sunTrips_1[i]>0) {
                DataValidationStopCt stopCt_1 = new DataValidationStopCt();
                stopCt_1.setStopCt(i);
                stopCt_1.setTripCts(new int[]
                    {tripTotals.wkdayTrips_1[i], tripTotals.satTrips_1[i], tripTotals.sunTrips_1[i]});
                stopCounts_1.add(stopCt_1);
              }
              if (stopCounts_1.size() > 0) {
                newDirCt_1.setStopCounts(stopCounts_1);
                newDirCountSet.add(newDirCt_1);
              }
            }
            if (newDirCountSet.size() > 0) {
              newHeadsignCt.setDirCounts(newDirCountSet);
              headsignCounts.add(newHeadsignCt);
            }
          }
          if (headsignCounts.size() > 0) {
            newRouteCts.setHeadsignCounts(headsignCounts);
            newModeRouteCts.add(newRouteCts);
          }
        }
      }
      if (newModeRouteCts.size() > 0) {
        newMode.setRoutes(newModeRouteCts);
        modes.add(newMode);
      }
    }
    return modes;
  }

  private LocalDate getFirstDay(int dayOfWeek, LocalDate startDate) {
    int old = startDate.getDayOfWeek();
    if (dayOfWeek < old) {
      dayOfWeek += 7;
    }
    return startDate.plusDays(dayOfWeek - old);
  }

  /**
   * Find the differences between two Lists of modes
   *
   * @param currentModes
   * @param selectedModes
   * @return
   */
  private List<DataValidationMode> findFixedRouteDiffs(
      List<DataValidationMode> currentModes,
      List<DataValidationMode> selectedModes) {

    List<DataValidationMode> fixedRouteDiffs = new ArrayList<>();
    if (currentModes != null && selectedModes == null) {
      return currentModes;
    }
    if (currentModes == null && selectedModes != null) {
      return selectedModes;
    }
    if (currentModes == null && selectedModes == null) {
      return fixedRouteDiffs;
    }

    for (DataValidationMode currentMode : currentModes) {
      // Check if this mode exists in selectedModes
      DataValidationMode diffMode = null;
      if (currentMode == null) continue;
      String modeName = currentMode.getModeName();
      for (DataValidationMode selectedMode : selectedModes) {
        if (modeName.equals(selectedMode.getModeName())) {
          selectedModes.remove(selectedMode);
          diffMode = compareModes(currentMode, selectedMode);
          break;
        }
      }
      if (diffMode == null && currentMode != null) {
        currentMode.setSrcCode("1");
        diffMode = currentMode;
      }
      if (diffMode.getRoutes().size() > 0) {
        fixedRouteDiffs.add(diffMode);
      }
    }
    if (selectedModes.size() > 0) {
      for (DataValidationMode selectedMode : selectedModes) {
        selectedMode.setSrcCode("2");
        fixedRouteDiffs.add(selectedMode);
      }
    }
    return fixedRouteDiffs;
  }

  private DataValidationMode compareModes(
      DataValidationMode currentMode, DataValidationMode selectedMode) {

    DataValidationMode diffMode = new DataValidationMode();
    diffMode.setModeName(currentMode.getModeName());
    diffMode.setRoutes(new TreeSet<DataValidationRouteCounts>());

    for (DataValidationRouteCounts currentRoute : currentMode.getRoutes()) {
      // Check if this route exists in selectedMode
      DataValidationRouteCounts diffRoute = null;
      String routeNum = currentRoute.getRouteNum();
      String routeName = currentRoute.getRouteName();
      for (DataValidationRouteCounts selectedRoute : selectedMode.getRoutes()) {
        if (routeNum.equals(selectedRoute.getRouteNum())) {
          selectedMode.getRoutes().remove(selectedRoute);
          if (routeName.equals(selectedRoute.getRouteName())) {
            diffRoute = compareRoutes(currentRoute, selectedRoute);
          } else {    // Route name changed, but not route number.
            currentRoute.setSrcCode("1");
            selectedRoute.setSrcCode("2");
            diffMode.getRoutes().add(currentRoute);
            diffRoute = selectedRoute;
          }
          break;
        }
      }
      if (diffRoute == null) {
        currentRoute.setSrcCode("1");
        diffRoute = currentRoute;
      }
      if (diffRoute.getHeadsignCounts().size() > 0) {
        diffMode.getRoutes().add(diffRoute);
      }
    }
    if (selectedMode.getRoutes().size() > 0) {
      for (DataValidationRouteCounts selectedRoute : selectedMode.getRoutes()) {
        selectedRoute.setSrcCode("2");
        diffMode.getRoutes().add(selectedRoute);
      }
    }
    return diffMode;
  }

  private DataValidationRouteCounts compareRoutes(
      DataValidationRouteCounts currentRoute, DataValidationRouteCounts selectedRoute) {
    DataValidationRouteCounts diffRoute = new DataValidationRouteCounts();

    diffRoute.setRouteName(currentRoute.getRouteName());
    diffRoute.setRouteNum(currentRoute.getRouteNum());
    diffRoute.setHeadsignCounts(new TreeSet<DataValidationHeadsignCts>());

    for (DataValidationHeadsignCts currentHeadsign : currentRoute.getHeadsignCounts()) {
      // Check if this headsign exists in selectedMode
      DataValidationHeadsignCts diffHeadsign = null;
      String headsignName = currentHeadsign.getHeadsign();
      for (DataValidationHeadsignCts selectedHeadsign : selectedRoute.getHeadsignCounts()) {
        if (headsignName.equals(selectedHeadsign.getHeadsign())) {
          selectedRoute.getHeadsignCounts().remove(selectedHeadsign);
          diffHeadsign = compareHeadsigns(currentHeadsign, selectedHeadsign);
          break;
        }
      }
      if (diffHeadsign == null) {
        currentHeadsign.setSrcCode("1");
        diffHeadsign = currentHeadsign;
      }
      if (diffHeadsign.getDirCounts().size() > 0) {
        diffRoute.getHeadsignCounts().add(diffHeadsign);
      }
    }
    if (selectedRoute.getHeadsignCounts().size() > 0) {
      for (DataValidationHeadsignCts selectedHeadsign : selectedRoute.getHeadsignCounts()) {
        selectedHeadsign.setSrcCode("2");
        diffRoute.getHeadsignCounts().add(selectedHeadsign);
      }
    }
    return diffRoute;
  }

  private DataValidationHeadsignCts compareHeadsigns (
      DataValidationHeadsignCts currentHeadsign, DataValidationHeadsignCts selectedHeadsign) {
    DataValidationHeadsignCts diffHeadsign = new DataValidationHeadsignCts();

    diffHeadsign.setHeadsign(currentHeadsign.getHeadsign());
    diffHeadsign.setDirCounts(new TreeSet<DataValidationDirectionCts>());

    for (DataValidationDirectionCts currentDirection : currentHeadsign.getDirCounts()) {
      // Check if this headsign exists in selectedMode
      DataValidationDirectionCts diffDirection = null;
      String directionName = currentDirection.getDirection();
      for (DataValidationDirectionCts selectedDirection : selectedHeadsign.getDirCounts()) {
        if (directionName.equals(selectedDirection.getDirection())) {
          selectedHeadsign.getDirCounts().remove(selectedDirection);
          diffDirection = compareDirections(currentDirection, selectedDirection);
          break;
        }
      }
      if (diffDirection == null) {
        currentDirection.setSrcCode("1");
        diffDirection = currentDirection;
      }
      if (diffDirection.getStopCounts().size() > 0) {
        diffHeadsign.getDirCounts().add(diffDirection);
      }
    }
    if (selectedHeadsign.getDirCounts().size() > 0) {
      for (DataValidationDirectionCts selectedDirection : selectedHeadsign.getDirCounts()) {
        selectedDirection.setSrcCode("2");
        diffHeadsign.getDirCounts().add(selectedDirection);
      }
    }
    return diffHeadsign;
  }

  private DataValidationDirectionCts compareDirections (
      DataValidationDirectionCts currentDirection, DataValidationDirectionCts selectedDirection) {

    DataValidationDirectionCts diffDirection = new DataValidationDirectionCts();
    diffDirection.setDirection(currentDirection.getDirection());
    diffDirection.setStopCounts(new TreeSet<DataValidationStopCt>());

    for (DataValidationStopCt currentStopCt : currentDirection.getStopCounts()) {
      boolean stopCtMatched = false;
      // Check if this stop count  exists in selectedMode
      for (DataValidationStopCt selectedStopCt : selectedDirection.getStopCounts()) {
        if (currentStopCt.getStopCt() == selectedStopCt.getStopCt()) {
          stopCtMatched = true;
          selectedDirection.getStopCounts().remove(selectedStopCt);
          if ((currentStopCt.getTripCts()[0] != selectedStopCt.getTripCts()[0])
              || (currentStopCt.getTripCts()[1] != selectedStopCt.getTripCts()[1])
              || (currentStopCt.getTripCts()[2] != selectedStopCt.getTripCts()[2])){
            currentStopCt.setSrcCode("1");
            diffDirection.getStopCounts().add(currentStopCt);
            selectedStopCt.setSrcCode("2");
            diffDirection.getStopCounts().add(selectedStopCt);
          }
          break;
        }
      }
      if (stopCtMatched) {
        continue;
      } else {
        currentStopCt.setSrcCode("1");
        diffDirection.getStopCounts().add(currentStopCt);
      }
    }
    if (selectedDirection.getStopCounts().size() > 0) {
      for (DataValidationStopCt selectedStopCt : selectedDirection.getStopCounts()) {
        selectedStopCt.setSrcCode("2");
        diffDirection.getStopCounts().add(selectedStopCt);
      }
    }
    return diffDirection;
  }

  private Map<String, List<String>> getReportModes() {
    Map<String, List<String>> reportModes = new HashMap<>();
    String sourceUrl = getSourceUrl();
    try (BufferedReader br =
        new BufferedReader(new InputStreamReader(new URL(sourceUrl).openStream()))) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] reportData = line.split(",");
        List<String> reportRoutes = reportModes.get(reportData[0]);
        if (reportRoutes == null) {
          reportRoutes = new ArrayList<>();
        }
        reportRoutes.add(reportData[1].trim());
        reportModes.put(reportData[0].trim(), reportRoutes);
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return reportModes;
  }

  /*
   * This method will use the config service to retrieve the URL for report
   * input parameters.  The value is stored in config.json.
   *
   * @return the URL to use to retrieve the modes and routes to be reported on
   */
  private String getSourceUrl() {
    String sourceUrl = "";

    try {
      List<Map<String, String>> components = _configurationServiceClient.getItems("config");
      if (components == null) {
        _log.info("getItems call failed");
      }
      for (Map<String, String> component: components) {
        if (component.containsKey("component") && "admin".equals(component.get("component"))) {
          if ("fixedRouteDataValidation".equals(component.get("key"))) {
             sourceUrl = component.get("value");
             break;
          }
        }
      }
    } catch (Exception e) {
      _log.error("could not retrieve Data Validation URL from config:", e);
    }

    return sourceUrl;
  }

  class TripTotals {
    int[] wkdayTrips_0;
    int[] wkdayTrips_1;
    int[] satTrips_0;
    int[] satTrips_1;
    int[] sunTrips_0;
    int[] sunTrips_1;

    public TripTotals () {
      wkdayTrips_0 = new int[MAX_STOP_CT+1];
      wkdayTrips_1 = new int[MAX_STOP_CT+1];
      satTrips_0 = new int[MAX_STOP_CT+1];
      satTrips_1 = new int[MAX_STOP_CT+1];
      sunTrips_0 = new int[MAX_STOP_CT+1];
      sunTrips_1 = new int[MAX_STOP_CT+1];
    }
  }

}
