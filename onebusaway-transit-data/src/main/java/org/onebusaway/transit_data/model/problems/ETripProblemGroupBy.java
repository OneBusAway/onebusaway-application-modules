/**
 * Copyright (C) 2012 Google, Inc.
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
 * When grouping {@link TripProblemReportBean} instances for summarization, as
 * collected in {@link TripProblemReportSummaryBean}, we can group problem
 * reports for counting in a couple of different ways. This enum describes those
 * methods.
 * 
 * @author bdferris
 * 
 */
public enum ETripProblemGroupBy {

  /**
   * Group problems by trip id, as returned by
   * {@link TripProblemReportBean#getTripId()}.
   */
  TRIP,

  /**
   * Group problems by {@link EProblemReportStatus}, as returned by
   * {@link TripProblemReportBean#getStatus()}.
   */
  STATUS,

  /**
   * Group problems by label, as returned by
   * {@link TripProblemReportBean#getLabel()}.
   */
  LABEL
}
