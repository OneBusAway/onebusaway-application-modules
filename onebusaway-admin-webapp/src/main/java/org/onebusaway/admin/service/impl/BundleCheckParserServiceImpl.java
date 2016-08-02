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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.onebusaway.admin.model.BundleValidationParseError;
import org.onebusaway.admin.model.BundleValidationParseResults;
import org.onebusaway.admin.model.ParsedBundleValidationCheck;
import org.onebusaway.admin.service.BundleCheckParserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Default implementation of {@link BundleCheckParserService} for parsing
 * a csv file.
 * @author jpearson
 *
 */
@Component
public class BundleCheckParserServiceImpl implements BundleCheckParserService {
  private static Logger _log = LoggerFactory.getLogger(BundleCheckParserServiceImpl.class);
  
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
  
  private static final String PARSE_ERROR = "Record did not contain a valid test type";
  
  public static final String[] TEST_VALUES = new String[] {
    TEST_ROUTE, 
    TEST_ROUTE_SEARCH, 
    TEST_SCHEDULE, 
    TEST_RT, 
    TEST_SCHEDULE_DATE, 
    TEST_DELETED_ROUTE_SEARCH, 
    TEST_ROUTE_REVISION, 
    TEST_SATURDAY_SCHEDULE, 
    TEST_SUNDAY_SCHEDULE, 
    TEST_WEEKDAY_SCHEDULE,
    TEST_EXPRESS_INDICATOR, 
    TEST_STOP_FOR_ROUTE, 
    TEST_NOT_STOP_FOR_ROUTE,
    TEST_STOP_DATE_AT_TIME,
    TEST_NOT_STOP_DATE_AT_TIME
    };
  public static final Set<String> validTests = new HashSet<String>(Arrays.asList(TEST_VALUES));
  
  @Override
  public BundleValidationParseResults parseBundleChecksFile(Reader csvDataFile) {
    // Create set of valid tests
    List<ParsedBundleValidationCheck> parsedChecks = new ArrayList<ParsedBundleValidationCheck>();
    List<BundleValidationParseError> parseErrors = new ArrayList<BundleValidationParseError> ();
    BundleValidationParseResults parseResults = new BundleValidationParseResults();
    parseResults.setParsedBundleChecks(parsedChecks);
    parseResults.setParseErrors(parseErrors);
    
    try {
      int linenum = 0;
      for (CSVRecord record : CSVFormat.DEFAULT.parse(csvDataFile)) {
        ++linenum;
        parseResults = parseRecord(record, parseResults);
      }
    }  catch (FileNotFoundException e) {
      _log.info("Exception parsing csv file ", e);
      e.printStackTrace();
    } catch (IOException e) {
      _log.info("Exception parsing csv file ", e);
      e.printStackTrace();
    }
    return parseResults;
  }
  private BundleValidationParseResults parseRecord(CSVRecord record, 
      BundleValidationParseResults parseResults) {  
    // Verify that second field contains a valid test.
    if (record.size() < 2 || !validTests.contains(record.get(1).toLowerCase())) {
      BundleValidationParseError parseError = new BundleValidationParseError();
      parseError.setLinenum((int)record.getRecordNumber());
      parseError.setErrorMessage(PARSE_ERROR);
      parseError.setOffendingLine(record.toString());
      parseResults.getParseErrors().add(parseError);
      return parseResults;  
    }
     
    ParsedBundleValidationCheck parsedCheck = new ParsedBundleValidationCheck();
    parsedCheck.setLinenum((int)record.getRecordNumber());
    parsedCheck.setAgencyId(record.get(0));
    parsedCheck.setSpecificTest(record.get(1));
    if (record.get(2) != null) {
      parsedCheck.setRouteName(record.get(2));
    }
    if (record.get(3) != null) {
      parsedCheck.setRouteId(record.get(3));
    }
    if (record.get(4) != null) {
      parsedCheck.setStopName(record.get(4));
    }
    if (record.get(5) != null) {
      parsedCheck.setStopId(record.get(5));
    }
    if (record.get(6) != null) {
      parsedCheck.setDate(record.get(6));
    }
    if (record.get(7) != null) {
      parsedCheck.setDepartureTime(record.get(7));
    }    
    parseResults.getParsedBundleChecks().add(parsedCheck);
    return parseResults;
  }
}
