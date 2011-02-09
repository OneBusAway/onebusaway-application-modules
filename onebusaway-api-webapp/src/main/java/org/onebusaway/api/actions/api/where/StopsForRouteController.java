package org.onebusaway.api.actions.api.where;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.api.model.transit.BeanFactoryV2;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;

public class StopsForRouteController extends ApiActionSupport {

  private static final long serialVersionUID = 1L;

  private static final int V1 = 1;

  private static final int V2 = 2;

  @Autowired
  private TransitDataService _service;

  private String _id;

  private boolean _includePolylines = true;

  public StopsForRouteController() {
    super(V1);
  }

  @RequiredFieldValidator
  public void setId(String id) {
    _id = id;
  }

  public String getId() {
    return _id;
  }
  
  
  public void setIncludePolylines(boolean includePolylines) {
    _includePolylines = includePolylines;
  }


  public DefaultHttpHeaders show() throws ServiceException {

    if (hasErrors())
      return setValidationErrorsResponse();

    StopsForRouteBean result = _service.getStopsForRoute(_id);

    if (result == null)
      return setResourceNotFoundResponse();

    if (isVersion(V1)) {
      return setOkResponse(result);
    } else if (isVersion(V2)) {
      BeanFactoryV2 factory = getBeanFactoryV2();
      return setOkResponse(factory.getResponse(result,_includePolylines));
    } else {
      return setUnknownVersionResponse();
    }
  }
}
