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

import org.onebusaway.admin.model.BundleValidateQuery;
import org.onebusaway.admin.model.BundleValidationCheckResult;

/**
 * 
 * This result checker will check the results from an api query searching for 
 * route information for a specific route and verify that the route is no
 * longer listed under the specified agency.
 * @author jpearson
 *
 */
public class DeletedRouteQueryResultChecker implements QueryResultChecker {

  @Override
  public BundleValidationCheckResult checkResults(BundleValidateQuery query) {
    BundleValidationCheckResult checkResult = new BundleValidationCheckResult();
    String result = query.getQueryResult();
    result = result.toLowerCase();
    String route = query.getRouteOrStop();
    route = route.toLowerCase();
    String routeId = query.getRouteId();
    if (routeId.length() == 0) {
      routeId = query.getRouteOrStop();
    }

    if (result.contains(CODE_200) && 
        (!result.contains(SHORT_NAME + route) && !result.contains(LONG_NAME + route))) {
      checkResult.setTestStatus(PASS);
      checkResult.setTestResult(query.getErrorMessage() + FOUND_ROUTE_INFO + routeId);
    } else {
      checkResult.setTestStatus(FAIL);
      checkResult.setTestResult(query.getErrorMessage() + DID_NOT_FIND_ROUTE_INFO + routeId);
    }       

    return checkResult;
  }

}
