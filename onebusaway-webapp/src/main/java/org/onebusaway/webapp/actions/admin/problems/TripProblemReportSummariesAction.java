package org.onebusaway.webapp.actions.admin.problems;

import java.util.Collections;
import java.util.List;

import org.apache.struts2.interceptor.validation.SkipValidation;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.problems.EProblemReportStatus;
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

  private List<TripProblemReportSummaryBean> _summaries;

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

  public List<TripProblemReportSummaryBean> getSummaries() {
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

    long t = System.currentTimeMillis();

    TripProblemReportQueryBean query = new TripProblemReportQueryBean();
    query.setAgencyId(_agencyId);
    query.setTimeFrom(0);
    query.setTimeTo(t);

    if (_days > 0)
      query.setTimeFrom(t - _days * 24 * 60 * 60 * 1000);

    if (_status != null)
      query.setStatus(EProblemReportStatus.valueOf(_status.toUpperCase()));

    ListBean<TripProblemReportSummaryBean> result = _transitDataService.getTripProblemReportSummaries(query);
    _summaries = result.getList();

    Collections.sort(_summaries);

    return SUCCESS;
  }
}
