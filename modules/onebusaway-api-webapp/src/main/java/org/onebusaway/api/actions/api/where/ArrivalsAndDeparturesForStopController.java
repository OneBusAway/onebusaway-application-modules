package org.onebusaway.api.actions.api.where;

import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.model.StopWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.services.TransitDataService;

import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

public class ArrivalsAndDeparturesForStopController extends ApiActionSupport {

  private static final long serialVersionUID = 1L;

  private static final String VERSION = "1.0";

  @Autowired
  private TransitDataService _service;

  private String _id;

  private Date _time;

  public ArrivalsAndDeparturesForStopController() {
    super(VERSION);
  }

  @RequiredFieldValidator(message = "whoa there")
  public void setId(String id) {
    _id = id;
  }

  public String getId() {
    return _id;
  }

  public void setTime(Date time) {
    _time = time;
  }

  public DefaultHttpHeaders show() throws ServiceException {

    if (hasErrors())
      return setValidationErrorsResponse();

    if (_time == null)
      _time = new Date();

    StopWithArrivalsAndDeparturesBean result = _service.getStopWithArrivalsAndDepartures(_id, _time);

    if (result == null)
      return setResourceNotFoundResponse();
    
    return setOkResponse(result);
  }
}
