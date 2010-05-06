package org.onebusaway.api.actions.api;

import org.onebusaway.api.model.ResponseBean;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;

import org.apache.struts2.rest.DefaultHttpHeaders;

import java.util.ArrayList;

public class ApiActionSupport extends ActionSupport implements ModelDriven<ResponseBean> {

  private static final long serialVersionUID = 1L;

  private String _version;

  private ResponseBean _response;
  
  public ApiActionSupport(String version) {
    _version = version;
  }

  public ResponseBean getModel() {
    return _response;
  }

  /*****************************************************************************
   * Response Bean Generation Methods
   ****************************************************************************/

  protected DefaultHttpHeaders setOkResponse(Object data) {
    _response = new ResponseBean(_version, ResponseCodes.RESPONSE_OK, "OK", data);
    return new DefaultHttpHeaders();
  }
  
  protected DefaultHttpHeaders setValidationErrorsResponse() {
      ValidationErrorBean bean = new ValidationErrorBean(new ArrayList<String>(getActionErrors()),getFieldErrors());
      _response = new ResponseBean(_version,ResponseCodes.RESPONSE_INVALID_ARGUMENT,"validation error",bean);
      return new DefaultHttpHeaders().withStatus(_response.getCode());
  }
  
  protected DefaultHttpHeaders setResourceNotFoundResponse() {
    _response = new ResponseBean(_version,ResponseCodes.RESPONSE_RESOURCE_NOT_FOUND,"resource not found",null);
    return new DefaultHttpHeaders().withStatus(_response.getCode());
  }
  
  protected DefaultHttpHeaders setExceptionResponse() {
    _response = new ResponseBean(_version,ResponseCodes.RESPONSE_SERVICE_EXCEPTION,"internal error",null);
    return new DefaultHttpHeaders().withStatus(_response.getCode());
  }

}
