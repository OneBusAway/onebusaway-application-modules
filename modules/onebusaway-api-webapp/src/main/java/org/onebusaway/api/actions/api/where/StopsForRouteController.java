package org.onebusaway.api.actions.api.where;

import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.transit_data.services.TransitDataService;

import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;

public class StopsForRouteController extends ApiActionSupport {

  private static final long serialVersionUID = 1L;

  private static final String VERSION = "1.0";

  @Autowired
  private TransitDataService _service;
  
  private String _id;

  public StopsForRouteController() {
    super(VERSION);
  }

  @RequiredFieldValidator
  public void setId(String id) {
    _id = id;
  }

  public String getId() {
    return _id;
  }

  public DefaultHttpHeaders show() {

    if (hasErrors())
      return setValidationErrorsResponse();

    StopsForRouteBean result = _service.getStopsForRoute(_id);
    
    if( result == null)
      return setResourceNotFoundResponse();
    
    return setOkResponse(result);
  }
}
