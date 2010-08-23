package org.onebusaway.api.actions.api.where;

import java.io.IOException;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.model.ReportProblemWithTripBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

public class ReportProblemWithTripController extends ApiActionSupport {

  private static final long serialVersionUID = 1L;

  @Autowired
  private TransitDataService _service;

  private ReportProblemWithTripBean _model = new ReportProblemWithTripBean();

  public ReportProblemWithTripController() {
    super(2);
  }

  public void setTripId(String tripId) {
    _model.setTripId(tripId);
  }

  public void setServiceDate(long serviceDate) {
    _model.setServiceDate(serviceDate);
  }

  public void setStopId(String stopId) {
    _model.setStopId(stopId);
  }

  public void setData(String data) {
    _model.setData(data);
  }

  public void setUserComment(String comment) {
    _model.setUserComment(comment);
  }

  public void setUserOnVehicle(boolean onVehicle) {
    _model.setUserOnVehicle(onVehicle);
  }

  public void setUserVehicleNumber(String vehicleNumber) {
    _model.setUserVehicleNumber(vehicleNumber);
  }

  public void setUserLat(double lat) {
    _model.setUserLat(lat);
  }

  public void setUserLon(double lon) {
    _model.setUserLon(lon);
  }
  
  public void setUserLocationAccuracy(double userLocationAccuracy) {
    _model.setUserLocationAccuracy(userLocationAccuracy);
  }

  public DefaultHttpHeaders index() throws IOException, ServiceException {

    if (hasErrors())
      return setValidationErrorsResponse();

    _model.setTime(System.currentTimeMillis());
    
    _service.reportProblemWithTrip(_model);

    return setOkResponse(null);
  }
}
