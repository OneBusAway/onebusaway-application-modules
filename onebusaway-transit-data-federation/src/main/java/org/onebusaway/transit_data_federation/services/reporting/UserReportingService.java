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

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.problems.ETripProblemGroupBy;
import org.onebusaway.transit_data.model.problems.StopProblemReportBean;
import org.onebusaway.transit_data.model.problems.StopProblemReportSummaryBean;
import org.onebusaway.transit_data.model.problems.StopProblemReportQueryBean;
import org.onebusaway.transit_data.model.problems.TripProblemReportBean;
import org.onebusaway.transit_data.model.problems.TripProblemReportSummaryBean;
import org.onebusaway.transit_data.model.problems.TripProblemReportQueryBean;

public interface UserReportingService {

  public void reportProblemWithStop(StopProblemReportBean problem);

  public ListBean<StopProblemReportSummaryBean> getStopProblemReportSummaries(
      StopProblemReportQueryBean query);

  public ListBean<StopProblemReportBean> getStopProblemReports(
      StopProblemReportQueryBean query);

  public List<StopProblemReportBean> getAllStopProblemReportsForStopId(
      AgencyAndId stopId);

  public StopProblemReportBean getStopProblemReportForId(long id);

  public void deleteStopProblemReportForId(long id);

  public void reportProblemWithTrip(TripProblemReportBean problem);

  public ListBean<TripProblemReportSummaryBean> getTripProblemReportSummaries(
      TripProblemReportQueryBean query, ETripProblemGroupBy groupBy);

  public ListBean<TripProblemReportBean> getTripProblemReports(
      TripProblemReportQueryBean query);

  public List<TripProblemReportBean> getAllTripProblemReportsForTripId(
      AgencyAndId tripId);

  public TripProblemReportBean getTripProblemReportForId(long id);

  public void updateTripProblemReport(TripProblemReportBean tripProblemReport);

  public void deleteTripProblemReportForId(long id);

  public List<String> getAllTripProblemReportLabels();
}
