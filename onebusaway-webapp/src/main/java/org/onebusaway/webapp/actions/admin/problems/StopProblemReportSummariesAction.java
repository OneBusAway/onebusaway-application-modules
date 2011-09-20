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
package org.onebusaway.webapp.actions.admin.problems;

import java.util.Collections;
import java.util.List;

import org.apache.struts2.interceptor.validation.SkipValidation;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.problems.EProblemReportStatus;
import org.onebusaway.transit_data.model.problems.StopProblemReportQueryBean;
import org.onebusaway.transit_data.model.problems.StopProblemReportSummaryBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;

public class StopProblemReportSummariesAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  private TransitDataService _transitDataService;

  private String _agencyId;

  private List<AgencyWithCoverageBean> _agencies;

  private List<StopProblemReportSummaryBean> _summaries;

  private String _status;

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

  public List<AgencyWithCoverageBean> getAgencies() {
    return _agencies;
  }

  public List<StopProblemReportSummaryBean> getSummaries() {
    return _summaries;
  }

  @SkipValidation
  @Override
  public String execute() {
    _agencies = _transitDataService.getAgenciesWithCoverage();
    return SUCCESS;
  }

  @Validations(requiredStrings = {@RequiredStringValidator(fieldName = "agencyId", message = "missing required agencyId field")})
  public String agency() {

    StopProblemReportQueryBean query = new StopProblemReportQueryBean();
    query.setAgencyId(_agencyId);
    query.setTimeFrom(0);
    query.setTimeTo(System.currentTimeMillis());
    if (_status != null)
      query.setStatus(EProblemReportStatus.valueOf(_status.toUpperCase()));

    ListBean<StopProblemReportSummaryBean> result = _transitDataService.getStopProblemReportSummaries(query);
    _summaries = result.getList();

    Collections.sort(_summaries);
    
    return SUCCESS;
  }
}
