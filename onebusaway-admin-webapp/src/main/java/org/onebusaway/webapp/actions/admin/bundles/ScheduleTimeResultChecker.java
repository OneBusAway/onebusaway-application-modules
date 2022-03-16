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
package org.onebusaway.webapp.actions.admin.bundles;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.MapperFeature;
import org.onebusaway.admin.model.BundleValidateQuery;
import org.onebusaway.admin.model.BundleValidationCheckResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * This result checker will check the results from an api query searching for 
 * schedule information for a specific stop.  It will check whether there is a
 * scheduled departure for that stop at the given time.
 * @author jpearson
 *
 */
public class ScheduleTimeResultChecker implements QueryResultChecker {
  private static Logger _log = LoggerFactory.getLogger(ScheduleTimeResultChecker.class);

  @Override
  public BundleValidationCheckResult checkResults(BundleValidateQuery query) {
    ObjectMapper mapper = new ObjectMapper();
    BundleValidationCheckResult checkResult = new BundleValidationCheckResult();
    String result = query.getQueryResult();
    mapper.configure(MapperFeature.AUTO_DETECT_FIELDS, true);
    Map<String, Object> parsedResult = new HashMap<String, Object>();
    boolean parseFailed = false;
    try {
      parsedResult = mapper.readValue(result, HashMap.class);
    } catch (JsonParseException e) {
      _log.error("JsonParseException trying to parse query results.");
      checkResult.setTestResult("JsonParseException trying to parse query results.");
      parseFailed = true;
    } catch (JsonMappingException e) {
      _log.error("JsonMappingException trying to parse query results.");
      checkResult.setTestResult("JsonMappingException trying to parse query results.");
      parseFailed = true;
    } catch (IOException e) {
      _log.error("IOException trying to parse query results.");
      checkResult.setTestResult("IOException trying to parse query results.");
      parseFailed = true;
    }
    if (parseFailed) {
      checkResult.setTestStatus(FAIL);
      return checkResult;
    }

    Map<String, Object> data = (Map<String, Object>) parsedResult.get("data");
    Map<String, Object> entry = (Map<String, Object>) data.get("entry");
    int httpCode = (Integer) parsedResult.get("code");    
    if (httpCode !=200) {     // Call failed or didn't find any schedule entries
      checkResult.setTestStatus(FAIL);
      checkResult.setTestResult("Http call failed with error " + httpCode);  
      //checkResult.setTestResult(DID_NOT_FIND_SCHEDULE_ENTRIES + "stop #" + query.getRouteOrStop());      
    } else {    
      // Parse the JSON result and create a set of the departure times
      Set<Long> departureTimes = new HashSet<Long> ();
      ArrayList<Map<String, Object>> stopRouteSchedules = (ArrayList<Map<String, Object>>) entry.get("stopRouteSchedules");
      for (Map<String, Object> stopRouteSchedule : stopRouteSchedules) {
        ArrayList<Map<String, Object>> stopRouteDirectionSchedules = (ArrayList<Map<String, Object>>) stopRouteSchedule.get("stopRouteDirectionSchedules");
        for (Map<String, Object> stopRouteDirectionSchedule : stopRouteDirectionSchedules) {
          ArrayList<Map<String, Object>> scheduleStopTimes = (ArrayList<Map<String, Object>>) stopRouteDirectionSchedule.get("scheduleStopTimes");
          for (Map<String, Object> scheduleStopTime : scheduleStopTimes) {
            // Check each departure time
            long departureTime =  (Long) scheduleStopTime.get("departureTime");
            departureTimes.add(departureTime);
          }
        }
      }
      // JSON successfully parsed, so continue processing
      Long timeInMillis = convertToTimeInMillis(query.getServiceDate(), query.getDepartureTime());
      long timeUpperBound = timeInMillis + (59 * 1000);  // In case GTFS time has some number of seconds
      long timeLowerBound = timeInMillis - (30 * 1000);  // In case GTFS time gets rounded up
      boolean foundTime = false;
      for (long departureTime : departureTimes) {
        if (departureTime >= timeLowerBound && departureTime <= timeUpperBound) {
          foundTime = true;
        }
      }
      if (foundTime) {
        checkResult.setTestResult(query.getErrorMessage() + FOUND_SCHEDULE_ENTRIES + query.getStopId() +" at departure time " + query.getDepartureTime());
        if (query.getSpecificTest().toLowerCase().equals("stop date at time")) {
          checkResult.setTestStatus(PASS);
        } else {
          checkResult.setTestStatus(FAIL);
        }
      } else {  // Didn't find a departure at that time
        checkResult.setTestResult(query.getErrorMessage() + DID_NOT_FIND_SCHEDULE_ENTRIES + query.getStopId() +" at departure time "
            + query.getDepartureTime());
        if (query.getSpecificTest().toLowerCase().equals("stop date at time")) {
          checkResult.setTestStatus(FAIL);
        } else {
          checkResult.setTestStatus(PASS);
        }
      }
    }
    return checkResult;
  }

  private Long convertToTimeInMillis(String serviceDate, String departureTime) {
    // Convert date and time to millis
    String dateAndTime = serviceDate + " " + departureTime;
    SimpleDateFormat sdf  = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
    Date date;
    try {
      date = sdf.parse(dateAndTime);
    } catch (ParseException e) {
      _log.error("ParseException parsing date " + dateAndTime);
      return 0L;
    }
    long timeInMillisSinceEpoch = date.getTime(); 
    // Adjust time if test is being run on the East coast
    if (TimeZone.getDefault().getID().equals("America/New_York")) {
      timeInMillisSinceEpoch += 3 * 60 * 60  * 1000;
    }
    return (Long)timeInMillisSinceEpoch;
  }
}
