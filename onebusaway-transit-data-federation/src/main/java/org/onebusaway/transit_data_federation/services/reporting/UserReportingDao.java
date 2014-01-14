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
package org.onebusaway.transit_data_federation.services.reporting;

import java.util.List;

import org.onebusaway.collections.tuple.T2;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.problems.EProblemReportStatus;
import org.onebusaway.transit_data.model.problems.ETripProblemGroupBy;
import org.onebusaway.transit_data.model.problems.TripProblemReportQueryBean;
import org.onebusaway.transit_data_federation.impl.reporting.StopProblemReportRecord;
import org.onebusaway.transit_data_federation.impl.reporting.TripProblemReportRecord;

public interface UserReportingDao {

  public void saveOrUpdate(Object record);

  public void delete(Object entity);

  public List<T2<AgencyAndId, Integer>> getStopProblemReportSummaries(
      String agencyId, long timeFrom, long timeTo, EProblemReportStatus status);

  public List<T2<Object, Integer>> getTripProblemReportSummaries(
      TripProblemReportQueryBean query, ETripProblemGroupBy groupBy);

  public List<StopProblemReportRecord> getStopProblemReports(String agencyId,
      long timeFrom, long timeTo, EProblemReportStatus status);

  public List<TripProblemReportRecord> getTripProblemReports(
      TripProblemReportQueryBean query);

  public List<StopProblemReportRecord> getAllStopProblemReportsForStopId(
      AgencyAndId stopId);

  public List<TripProblemReportRecord> getAllTripProblemReportsForTripId(
      AgencyAndId tripId);

  public StopProblemReportRecord getStopProblemRecordForId(long id);

  public TripProblemReportRecord getTripProblemRecordForId(long id);

  public List<String> getAllTripProblemReportLabels();
}
