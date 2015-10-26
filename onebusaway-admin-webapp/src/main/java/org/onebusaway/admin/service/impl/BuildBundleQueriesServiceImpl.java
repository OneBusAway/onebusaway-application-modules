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
package org.onebusaway.admin.service.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.onebusaway.admin.model.BundleValidateQuery;
import org.onebusaway.admin.model.ParsedBundleValidationCheck;
import org.onebusaway.admin.service.BuildBundleQueriesService;
import org.onebusaway.util.services.configuration.ConfigurationServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * For building the api queries that will be used to check the validity of a transit data bundle.
 * The queries are generated from parsed data returned from BundleCheckParserService.
 * @author jpearson
 *
 */
@Component
public class BuildBundleQueriesServiceImpl implements BuildBundleQueriesService {
  private static Logger _log = LoggerFactory.getLogger(BuildBundleQueriesServiceImpl.class);

  private static final String STAGING_ENV = "staging";
  private static final String PRODUCTION_ENV = "prod";

  // Names of valid tests
  private static final String TEST_ROUTE = "route";
  private static final String TEST_ROUTE_SEARCH = "route search";
  private static final String TEST_SCHEDULE = "schedule";
  private static final String TEST_RT = "rt";
  private static final String TEST_SCHEDULE_DATE = "schedule-date";
  private static final String TEST_DELETED_ROUTE_SEARCH = "deleted route search";
  private static final String TEST_ROUTE_REVISION = "route revision";
  private static final String TEST_SATURDAY_SCHEDULE = "saturday schedule";
  private static final String TEST_SUNDAY_SCHEDULE = "sunday schedule";
  private static final String TEST_EXPRESS_INDICATOR = "express indicator";
  private static final String TEST_STOP_FOR_ROUTE = "stop for route";
  private static final String TEST_NOT_STOP_FOR_ROUTE = "not stop for route";
  private static final String TEST_STOP_DATE_AT_TIME = "stop date at time";
  private static final String TEST_NOT_STOP_DATE_AT_TIME = "not stop date at time";

  private static final String DATE_FLD = "date=";

  private static final String SCHEDULE_FOR_STOP = "/where/schedule-for-stop/";
  private static final String STOP_MONITORING = "/stop-monitoring?";
  private static final String ROUTE = "/where/route/";
  private static final String ROUTES_FOR_AGENCY = "/where/routes-for-agency/";
  private static final String ARRIVALS_AND_DEPARTURES_FOR_STOP = "/where/arrivals-and-departures-for-stop/";
  private static final String STOP = "/where/stop/";

  @Autowired
  private ConfigurationServiceClient _configurationServiceClient;

  @Override
  public List<BundleValidateQuery> buildQueries(List<ParsedBundleValidationCheck> parsedChecks,
      String checkEnvironment) {

    List<BundleValidateQuery> queries = new ArrayList<BundleValidateQuery>();
    String envURI = "";
    if (checkEnvironment.equals(PRODUCTION_ENV)) {
      envURI = getConfigValue("apiProd");
    } else {
      envURI = getConfigValue("apiStaging");
    }
    String apiKey = getConfigValue("apiKey");
    String apiQuery = getConfigValue("apiQuery");
    String siriQuery = getConfigValue("siriQuery");

    for (ParsedBundleValidationCheck check : parsedChecks) {
      BundleValidateQuery validationQuery = new BundleValidateQuery();
      String specificTest = check.getSpecificTest().toLowerCase();
      String query = envURI;
      String stopId = check.getStopId();
      String routeId = check.getRouteId();
      if (specificTest.equals(TEST_SCHEDULE) || specificTest.equals(TEST_SCHEDULE_DATE)
          || specificTest.equals(TEST_SATURDAY_SCHEDULE) || specificTest.equals(TEST_SUNDAY_SCHEDULE)
          || specificTest.equals(TEST_STOP_DATE_AT_TIME) || specificTest.equals(TEST_NOT_STOP_DATE_AT_TIME)) {
        String date = "";
        if (specificTest.equals(TEST_SCHEDULE_DATE) || specificTest.equals(TEST_STOP_DATE_AT_TIME) 
            || specificTest.equals(TEST_NOT_STOP_DATE_AT_TIME)) {
          date = DATE_FLD + check.getDate();
          date += "&";
        } else if (specificTest.equals(TEST_SATURDAY_SCHEDULE)) {
          date = DATE_FLD + getNextDayOfWeek(Calendar.SATURDAY);
          date += "&";
        } else if (specificTest.equals(TEST_SUNDAY_SCHEDULE)) {
          date = DATE_FLD + getNextDayOfWeek(Calendar.SUNDAY);
          date += "&";
        }
        query += apiQuery + SCHEDULE_FOR_STOP + stopId + ".json?" + date + "key=" + apiKey + "&version=2";
      } else if (specificTest.equals(TEST_RT)) {
        query += siriQuery + STOP_MONITORING + "key=" + apiKey + "&MonitoringRef=" + stopId + "&type=json";
      } else if (specificTest.equals(TEST_ROUTE) || specificTest.equals(TEST_ROUTE_REVISION)) {
        query += apiQuery + ROUTE + routeId + ".json?key=" + apiKey + "&version=2";
      } else if (specificTest.equals(TEST_ROUTE_SEARCH) || specificTest.equals(TEST_DELETED_ROUTE_SEARCH)) {
        String agency = check.getAgencyId();
        query += apiQuery + ROUTES_FOR_AGENCY + agency + ".json?key=" + apiKey + "&version=2";
      } else if (specificTest.equals(TEST_EXPRESS_INDICATOR)) {
        query += apiQuery + ARRIVALS_AND_DEPARTURES_FOR_STOP + stopId + ".json?key=" + apiKey + "&version=2";
      } else if (specificTest.equals(TEST_EXPRESS_INDICATOR)) {
        query += apiQuery + ARRIVALS_AND_DEPARTURES_FOR_STOP + stopId + ".json?key=" + apiKey + "&version=2";
      } else if (specificTest.equals(TEST_STOP_FOR_ROUTE) || specificTest.equals(TEST_NOT_STOP_FOR_ROUTE)) {
        query += apiQuery + STOP + stopId + ".json?key=" + apiKey + "&version=2"; 
      } else {
        continue;
      }      
      validationQuery.setLinenum(check.getLinenum());
      validationQuery.setSpecificTest(check.getSpecificTest());
      validationQuery.setRouteOrStop(check.getRouteName());
      validationQuery.setRouteId(check.getRouteId());
      validationQuery.setStopId(check.getStopId());
      validationQuery.setServiceDate(check.getDate());
      validationQuery.setDepartureTime(check.getDepartureTime());
      validationQuery.setQuery(query);
      queries.add(validationQuery);      
    }

    return queries;
  }

  /* Private methods */

  private String getConfigValue(String configKey) {
    String value = "";

    try {
      List<Map<String, String>> components = _configurationServiceClient.getItems("config");
      if (components == null) {
        _log.info("getItems call failed");
      }
      for (Map<String, String> component: components) {
        if (configKey.equals(component.get("key"))) {
          value = component.get("value");
        }
      }
    } catch (Exception e) {
      _log.error("Exception while trying to get environment host");
      e.printStackTrace();
    }
    return value;
  }

  private static String getNextDayOfWeek(int dayOfWeek) {
    // Get next specified day of week
    Calendar c = Calendar.getInstance();
    c.set(Calendar.DAY_OF_WEEK, dayOfWeek);
    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    return df.format(c.getTime());
  }  
}
