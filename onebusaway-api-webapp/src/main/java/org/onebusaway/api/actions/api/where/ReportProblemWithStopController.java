package org.onebusaway.api.actions.api.where;

import java.io.IOException;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.model.problems.EProblemReportStatus;
import org.onebusaway.transit_data.model.problems.StopProblemReportBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;

public class ReportProblemWithStopController extends ApiActionSupport {

  private static final long serialVersionUID = 1L;

  @Autowired
  private TransitDataService _service;

  private StopProblemReportBean _model = new StopProblemReportBean();

  public ReportProblemWithStopController() {
    super(2);
  }

  @RequiredStringValidator(message="requiredField.stopId")
  public void setStopId(String stopId) {
    _model.setStopId(stopId);
  }
  
  public String getStopId() {
    return _model.getStopId();
  }

  public void setData(String data) {
    _model.setData(data);
  }

  public void setUserComment(String userComment) {
    _model.setUserComment(userComment);
  }

  public void setUserLat(double userLat) {
    _model.setUserLat(userLat);
  }

  public void setUserLon(double userLon) {
    _model.setUserLon(userLon);
  }

  public void setUserLocationAccuracy(double userLocationAccuracy) {
    _model.setUserLocationAccuracy(userLocationAccuracy);
  }

  public DefaultHttpHeaders create() throws IOException, ServiceException {
    return index();
  }

  public DefaultHttpHeaders index() throws IOException, ServiceException {

    if (hasErrors())
      return setValidationErrorsResponse();

    _model.setTime(System.currentTimeMillis());
    _model.setStatus(EProblemReportStatus.NEW);
    _service.reportProblemWithStop(_model);

    return setOkResponse(new Object());
  }
}
