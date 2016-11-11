/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
package org.onebusaway.admin.service.bundle.task;

import java.io.BufferedReader;
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

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.transit_data_federation.bundle.tasks.MultiCSVLogger;
import org.onebusaway.transit_data_federation.services.FederatedTransitDataBundle;
import org.onebusaway.util.services.configuration.ConfigurationServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class FixedRouteDataValidationTask implements Runnable {
  private static final String FILENAME = "fixed_route_validation.csv";
  private Logger _log = LoggerFactory.getLogger(FixedRouteDataValidationTask.class);
  private MultiCSVLogger logger;
  private GtfsMutableRelationalDao _dao;
  private FederatedTransitDataBundle _bundle;
  private static final int MAX_STOP_CT = 150;
  int maxStops = 0;

  @Autowired
  public void setLogger(MultiCSVLogger logger) {
    this.logger = logger;
  }

  @Autowired
  public void setGtfsDao(GtfsMutableRelationalDao dao) {
    _dao = dao;
  }

  @Autowired
  public void setBundle(FederatedTransitDataBundle bundle) {
    _bundle = bundle;
  }

  @Autowired
  private ConfigurationServiceClient _configurationServiceClient;

  @Override
  public void run() {
    if (StringUtils.isBlank(getSourceUrl())) {
      _log.info("missing sourceUrl, exiting");
      return;
    }
    try {
      process();
      _log.info("done");
    } catch (Exception e) {
      _log.error("exception with FixedRouteDataValidationTask:", e);    
    }
    
  }

  private void process() throws Exception {

    _log.info("Creating fixed route data validation report with sourceUrl=" + getSourceUrl());
    logger.header(FILENAME, "Mode,Route,Headsign,Direction,# of stops,# of weekday trips,# of Sat trips,# of Sunday trips");

    // Use next Wednesday date (including today) to serve as weekday check date.
    LocalDate firstMon = getFirstDay(DateTimeConstants.MONDAY);
    LocalDate firstTues = getFirstDay(DateTimeConstants.TUESDAY);
    LocalDate firstWed = getFirstDay(DateTimeConstants.WEDNESDAY);
    LocalDate firstThur = getFirstDay(DateTimeConstants.THURSDAY);
    LocalDate firstFri = getFirstDay(DateTimeConstants.FRIDAY);
    LocalDate firstSat = getFirstDay(DateTimeConstants.SATURDAY);
    LocalDate firstSun = getFirstDay(DateTimeConstants.SUNDAY);

    // Get the service ids for weekdays, Saturdays, and Sundays
    Set<AgencyAndId> weekdaySvcIds = new HashSet<>();
    Set<AgencyAndId> saturdaySvcIds = new HashSet<>();
    Set<AgencyAndId> sundaySvcIds = new HashSet<>();

    // Check service ids
    Collection<ServiceCalendar> calendars = _dao.getAllCalendars();
    for (ServiceCalendar calendar : calendars) {
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
    Collection<Agency> agencies = _dao.getAllAgencies();
    for (String currentMode : reportModes.keySet()) {
      List<String> currentRoutes = reportModes.get(currentMode);
      for (Agency agency : agencies) {
        boolean getAllRoutes = false;
        // If currentRoutes[0] is agency id, get all the routes for that agency
        if (currentRoutes.get(0).equals(agency.getId())) {
          getAllRoutes = true;
        }
        List<Route> routes = _dao.getRoutesForAgency(agency);
        for (Route route : routes) {
          int[] wkdayTrips = null;
          int[] satTrips = null;
          int[] sunTrips = null;
          Map<String, TripTotals> tripMap = new HashMap<>();
          AgencyAndId routeId = route.getId();
          if (currentRoutes.contains(routeId.toString())
              || getAllRoutes) {
            List<Trip> trips = _dao.getTripsForRoute(route);
            for (Trip trip : trips) {
              List<StopTime> stopTimes =  _dao.getStopTimesForTrip(trip);
              int stopCt = stopTimes.size();
              if (stopCt > MAX_STOP_CT) {
                stopCt = MAX_STOP_CT;
              }
              TripTotals tripTotals = null;
              if (tripMap.containsKey(trip.getTripHeadsign())) {
                tripTotals = tripMap.get(trip.getTripHeadsign());
              } else {
                tripTotals = new TripTotals();
                tripMap.put(trip.getTripHeadsign(), tripTotals);
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
              AgencyAndId tripSvcId = trip.getServiceId();
              if (weekdaySvcIds.contains(tripSvcId)) {
                ++wkdayTrips[stopCt];
              } else if (saturdaySvcIds.contains(tripSvcId)) {
                ++satTrips[stopCt];
              } else if (sundaySvcIds.contains(tripSvcId)) {
                ++sunTrips[stopCt];
              }
              tripMap.put(trip.getTripHeadsign(), tripTotals);
            }
            String routeName = route.getShortName() + "-" + route.getDesc();
            for (String headSign : tripMap.keySet() ) {
              TripTotals tripTotals = tripMap.get(headSign);
              String dir_0 = "0";
              String dir_1 = "1";
              for (int i=0; i<MAX_STOP_CT; ++i) {
                if (tripTotals.wkdayTrips_0[i]>0
                    || tripTotals.satTrips_0[i]>0
                    || tripTotals.sunTrips_0[i]>0) {
                  logger.logCSV(FILENAME, currentMode + "," + routeName + ","
                      + headSign + "," + dir_0 + ","
                      + i + "," + tripTotals.wkdayTrips_0[i] + ","
                      + tripTotals.satTrips_0[i] + ","
                      + tripTotals.sunTrips_0[i]);
                  dir_0 = "";       // Only display direction on its first line
                  headSign = "";    // Only display headsign on its first line
                  routeName = "";   // Only display route on its first line
                  currentMode = "";  // Only display mode on its first line
                }
              }
              for (int i=0; i<MAX_STOP_CT; ++i) {
                if (tripTotals.wkdayTrips_1[i]>0
                    || tripTotals.satTrips_1[i]>0
                    || tripTotals.sunTrips_1[i]>0) {
                  logger.logCSV(FILENAME, currentMode + "," + routeName + ","
                      + headSign + "," + dir_1 + ","
                      + i + "," + tripTotals.wkdayTrips_1[i] + ","
                      + tripTotals.satTrips_1[i] + ","
                      + tripTotals.sunTrips_1[i]);
                  dir_1 = "";       // Only display direction on its first line
                  headSign = "";    // Only display headsign on its first line
                  routeName = "";   // Only display route on its first line
                  currentMode = "";  // Only display mode on its first line
                }
              }
            }
          }
        }
      }
      logger.logCSV(FILENAME,",,,,,,,,");
    }
    _log.info("finished fixed route data validation report");
  }
  
  private LocalDate getFirstDay(int dayOfWeek) {
    LocalDate today = LocalDate.now();
    int old = today.getDayOfWeek();
    if (dayOfWeek < old) {
      dayOfWeek += 7;
    }
    return today.plusDays(dayOfWeek - old);
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
