/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.transit_data.model.problems;

/**
 * Status of a problem report, similar the the status of a bug in an issue
 * tracking system.
 * 
 * @author bdferris
 * @see StopProblemReportBean
 * @see TripProblemReportBean
 */
public enum EProblemReportStatus {

  /**
   * A new, unaccepted problem report
   */
  NEW,

  /**
   * An accepted problem report that needs action
   */
  ACCEPTED,
  
  /**
   * Work on this problem has been started
   */
  STARTED,
  
  /**

  /**
   * A problem report that has been fixed
   */
  FIXED,

  /**
   * The fix for the problem report has been verified
   */
  VERIFIED,
  
  /**
   * The problem report is invalid
   */
  INVALID,
  
  /**
   * Duplicate error report
   */
  DUPLICATE,

  /**
   * We will not address this problem report
   */
  WONT_FIX
}
