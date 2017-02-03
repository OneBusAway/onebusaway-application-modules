/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.webapp.actions.admin.problems;

import java.util.Collections;
import java.util.List;

import org.apache.struts2.interceptor.validation.SkipValidation;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.problems.EProblemReportStatus;
import org.onebusaway.transit_data.model.problems.ETripProblemGroupBy;
import org.onebusaway.transit_data.model.problems.TripProblemReportQueryBean;
import org.onebusaway.transit_data.model.problems.TripProblemReportSummaryBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;

public class TripProblemReportSummariesAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  private TransitDataService _transitDataService;

  private String _agencyId;

  private List<AgencyWithCoverageBean> _agencies;

  private List<TripProblemReportSummaryBean> _summariesByTrip;

  private List<TripProblemReportSummaryBean> _summariesByLabel;

  private String _status;

  private int _days = 0;

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  public void setAgencyId(String agencyId) {
    _agencyId = agencyId;
  }

  public String getAgencyId() {
    return _agencyId;
  }

  public void setStatus(String status) {
    _status = status;
  }

  public String getStatus() {
    return _status;
  }

  public void setDays(int days) {
    _days = days;
  }

  public List<AgencyWithCoverageBean> getAgencies() {
    return _agencies;
  }

  public List<TripProblemReportSummaryBean> getSummariesByTrip() {
    return _summariesByTrip;
  }

  public List<TripProblemReportSummaryBean> getSummariesByLabel() {
    return _summariesByLabel;
  }

  @SkipValidation
  @Override
  public String execute() {
    _agencies = _transitDataService.getAgenciesWithCoverage();
    return SUCCESS;
  }

  @Validations(requiredStrings = {@RequiredStringValidator(fieldName = "agencyId", message = "missing required agencyId field")})
  public String agency() {

    long t = System.currentTimeMillis();

    TripProblemReportQueryBean query = new TripProblemReportQueryBean();
    query.setAgencyId(_agencyId);
    query.setTimeFrom(0);
    query.setTimeTo(t);

    if (_days > 0)
      query.setTimeFrom(t - _days * 24 * 60 * 60 * 1000);

    if (_status != null)
      query.setStatus(EProblemReportStatus.valueOf(_status.toUpperCase()));

    ListBean<TripProblemReportSummaryBean> resultByTrip = _transitDataService.getTripProblemReportSummariesByGrouping(
        query, ETripProblemGroupBy.TRIP);
    _summariesByTrip = resultByTrip.getList();

    ListBean<TripProblemReportSummaryBean> resultByLabel = _transitDataService.getTripProblemReportSummariesByGrouping(
        query, ETripProblemGroupBy.LABEL);
    _summariesByLabel = resultByLabel.getList();

    Collections.sort(_summariesByTrip);
    Collections.sort(_summariesByLabel);

    return SUCCESS;
  }
}
