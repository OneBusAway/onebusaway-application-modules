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
/**
 * 
 */
package org.onebusaway.webapp.actions.admin.bundles;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
 * schedule information for a specific stop to see if that stop
 * @author jpearson
 *
 */
public class StopForRouteResultChecker implements QueryResultChecker {
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
    
    // JSON successfully parsed, so continue processing
    int httpCode = (Integer) parsedResult.get("code");
    Map<String, Object> data = (Map<String, Object>) parsedResult.get("data");
    Map<String, Object> entry = (Map<String, Object>) data.get("entry");
    ArrayList<Object> routeIds = (ArrayList<Object>) entry.get("routeIds");
    if (httpCode != 200 || routeIds == null || routeIds.size() == 0) {  
      checkResult.setTestStatus(FAIL);  // Call failed or didn't find any route entries
      checkResult.setTestResult(query.getErrorMessage() + "Did not find any routes for stop #" + query.getStopId());      
    } else {  // Succeeded at finding stop info, but does it include this route?
      if (routeIds.contains(query.getRouteId())) {
        checkResult.setTestResult(query.getErrorMessage() + "Found stop #" + query.getStopId() 
            + " on Route #" + query.getRouteId());
        if (query.getSpecificTest().toLowerCase().equals("stop for route")) {
          checkResult.setTestStatus(PASS);
        } else {
          checkResult.setTestStatus(FAIL);
        }
      } else {  // Didn't find that route for that stop
        checkResult.setTestResult(query.getErrorMessage() + "Did not find stop #" + query.getStopId() 
            + " on Route #" + query.getRouteId());
        if (query.getSpecificTest().toLowerCase().equals("stop date at time")) {
          checkResult.setTestStatus(FAIL);
        } else {
          checkResult.setTestStatus(PASS);
        }
      }
    }
    return checkResult;
  }  
}
