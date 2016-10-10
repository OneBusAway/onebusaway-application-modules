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

    _log.info("Creating fixed route data validation report with sourceUrl=" + getSourceUrl());
    logger.header(FILENAME, "Mode,Route,# of stops,# of weekday trips,# of Sat trips,# of Sunday trips");   
    
    // Use next Wednesday date (including today) to serve as weekday check date.
    LocalDate firstWed = getFirstDay(DateTimeConstants.WEDNESDAY);
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
      if (calendar.getWednesday() == 1 && !firstWed.isBefore(jodaStartDate)
          && !firstWed.isAfter(jodaEndDate)) {
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
          int[] wkdayTrips = new int[100];
          int[] satTrips = new int[100];
          int[] sunTrips = new int[100];
          AgencyAndId routeId = route.getId();
          if (currentRoutes.contains(routeId.toString())
              || getAllRoutes) {
            List<Trip> trips = _dao.getTripsForRoute(route);
            for (Trip trip : trips) {
              List<StopTime> stopTimes =  _dao.getStopTimesForTrip(trip);
              int stopCt = stopTimes.size();
              /*
               * TODO: if stopCt exceeds array sizes, resize arrays
               */
              AgencyAndId tripSvcId = trip.getServiceId();
              if (weekdaySvcIds.contains(tripSvcId)) {
                ++wkdayTrips[stopCt];
              } else if (saturdaySvcIds.contains(tripSvcId)) {
                ++satTrips[stopCt];
              } else if (sundaySvcIds.contains(tripSvcId)) {
                ++sunTrips[stopCt];
              }
            }
            String routeName = route.getDesc();
            if (routeName == null || routeName.length() > 40) {
              routeName = route.getLongName();
            }
            if (routeName == null || routeName.isEmpty() || routeName.length() > 40) {
              routeName = route.getShortName();
            }
            if (routeName == null || routeName.isEmpty() || routeName.length() > 40) {
              routeName = route.getDesc();
              if (routeName != null) {
                routeName = routeName.substring(0, 40);
              }
            }
            if (routeName == null) {
              routeName = "";
            }
            for (int i=0; i<wkdayTrips.length; ++i) {
              if (wkdayTrips[i]>0 || satTrips[i] > 0 || sunTrips[i]>0) {
                logger.logCSV(FILENAME, currentMode + "," + routeName + ","
                    + i + "," + wkdayTrips[i] + ","
                    + satTrips[i] + "," + sunTrips[i]);
                routeName = "";
                currentMode = "";  // Only display mode on its first line
              }
            }
          }
        }
      }
      logger.logCSV(FILENAME,",,,,,,");
    }
    _log.info("done");
  }
  
  private LocalDate getFirstDay(int dayOfWeek) {
    LocalDate today = LocalDate.now();
    int old = today.getDayOfWeek();
    if (dayOfWeek < old) {
      dayOfWeek += 7;
    }
    return today.plusDays(dayOfWeek - old);
  }
  private LocalDate getFirstSat() {
    LocalDate today = LocalDate.now();
    int old = today.getDayOfWeek();
    int saturday = 6;
    if (saturday < old) {
      saturday += 7;
    }
    return today.plusDays(saturday - old);
  }
  
  private Map<String, List<String>> getReportModes() {
    Map<String, List<String>> reportModes = new HashMap<>();
    //String sourceURL = "https://raw.github.com/wiki/camsys/onebusaway-application-modules/FixedRouteDataValidation.md";
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
        reportRoutes.add(reportData[1]);
        reportModes.put(reportData[0], reportRoutes);
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
}
