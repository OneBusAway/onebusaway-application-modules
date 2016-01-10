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

import java.util.List;

import org.onebusaway.admin.model.BundleValidateQuery;
import org.onebusaway.admin.model.BundleValidationCheckResult;

/**
 * 
 * This is the interface to a set of classes, each of which will evaluate the
 * result returned from a specific type of query used for validating a
 * transit data bundle.
 * @author jpearson
 *
 */
public interface QueryResultChecker {
  public static final String SHORT_NAME = "shortname\":\"";
  public static final String LONG_NAME = "longname\":\"";
  
  // Result messages
  public static final String FOUND_SCHEDULE_ENTRIES = "Found schedule entries for stop ";
  public static final String DID_NOT_FIND_SCHEDULE_ENTRIES = "Did not find schedule entries for stop ";
  public static final String FOUND_REALTIME_INFO = "Found real time info for stop ";
  public static final String DID_NOT_FIND_REALTIME_INFO = "Did not find real time info for stop ";
  public static final String FOUND_ROUTE_INFO = "Found information for route ";
  public static final String DID_NOT_FIND_ROUTE_INFO = "Did not find information for route ";
  
  public static final String PASS = "Pass";
  public static final String FAIL = "Fail";
  public static final String CODE_200 = "\"code\":200";

  public BundleValidationCheckResult checkResults(BundleValidateQuery query);
}
