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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.admin.model.BundleValidateQuery;
import org.onebusaway.admin.model.ParsedBundleValidationCheck;
import org.onebusaway.admin.service.BuildBundleQueriesService;
import org.onebusaway.admin.service.BundleValidationCheckService;
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
  private static final String TEST_WEEKDAY_SCHEDULE = "weekday schedule";
  private static final String TEST_EXPRESS_INDICATOR = "express indicator";
  private static final String TEST_STOP_FOR_ROUTE = "stop for route";
  private static final String TEST_NOT_STOP_FOR_ROUTE = "not stop for route";
  private static final String TEST_STOP_DATE_AT_TIME = "stop date at time";
  private static final String TEST_NOT_STOP_DATE_AT_TIME = "not stop date at time";

  Map<String, BundleValidationCheckService> queryBuilders = null;

  @Autowired
  private ConfigurationServiceClient _configurationServiceClient;

  @Override
  public List<BundleValidateQuery> buildQueries(List<ParsedBundleValidationCheck> parsedChecks,
      String checkEnvironment) {

    if (queryBuilders == null) {
      initQueryBuilders();
    }

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
      String query = queryBuilders.get(specificTest) == null ? "" :
        queryBuilders.get(specificTest).buildQuery(envURI, apiKey,
            apiQuery, siriQuery, check);
      validationQuery.setLinenum(check.getLinenum());
      validationQuery.setSpecificTest(check.getSpecificTest());
      validationQuery.setRouteOrStop(check.getRouteName());
      validationQuery.setRouteId(check.getRouteId());
      validationQuery.setStopId(check.getStopId());
      validationQuery.setServiceDate(check.getDate());
      validationQuery.setDepartureTime(check.getDepartureTime());
      validationQuery.setQuery(query);
      if (query.isEmpty()) {
        continue;
      } else {
        queries.add(validationQuery);
      }
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

  private void initQueryBuilders() {
    queryBuilders = new HashMap<String, BundleValidationCheckService>();
    queryBuilders.put(TEST_SCHEDULE, new ScheduleCheckServiceImpl());
    queryBuilders.put(TEST_SCHEDULE_DATE, new ScheduleDateCheckServiceImpl());
    queryBuilders.put(TEST_SATURDAY_SCHEDULE, new SaturdayScheduleCheckServiceImpl());
    queryBuilders.put(TEST_SUNDAY_SCHEDULE, new SundayScheduleCheckServiceImpl());
    queryBuilders.put(TEST_WEEKDAY_SCHEDULE, new WeekdayScheduleCheckServiceImpl());
    queryBuilders.put(TEST_STOP_DATE_AT_TIME, new StopDateAtTimeCheckServiceImpl());
    queryBuilders.put(TEST_NOT_STOP_DATE_AT_TIME, new NotStopdateAtTimeCheckServiceImpl());
    queryBuilders.put(TEST_RT, new RtCheckServiceImpl());
    queryBuilders.put(TEST_ROUTE, new RouteCheckServiceImpl());
    queryBuilders.put(TEST_ROUTE_REVISION, new RouteRevisionCheckServiceImpl());
    queryBuilders.put(TEST_ROUTE_SEARCH, new RouteSearchCheckServiceImpl());
    queryBuilders.put(TEST_DELETED_ROUTE_SEARCH, new DeletedRouteSearchCheckServiceImpl());
    queryBuilders.put(TEST_EXPRESS_INDICATOR, new ExpressIndicatorCheckServiceImpl());
    queryBuilders.put(TEST_STOP_FOR_ROUTE, new StopForRouteCheckServiceImpl());
    queryBuilders.put(TEST_NOT_STOP_FOR_ROUTE, new NotStopForRouteCheckServiceImpl());
 }
}
