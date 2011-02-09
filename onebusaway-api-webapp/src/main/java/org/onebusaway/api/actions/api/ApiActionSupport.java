package org.onebusaway.api.actions.api;

import org.onebusaway.api.impl.MaxCountSupport;
import org.onebusaway.api.model.ResponseBean;
import org.onebusaway.api.model.transit.BeanFactoryV2;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;

import org.apache.struts2.rest.DefaultHttpHeaders;

import java.util.ArrayList;

public class ApiActionSupport extends ActionSupport implements
    ModelDriven<ResponseBean> {

  private static final long serialVersionUID = 1L;
  
  private static final int NO_VERSION = -999;

  private int _defaultVersion;

  private ResponseBean _response;

  private int _version = -999;

  private boolean _includeReferences = true;

  public ApiActionSupport(int defaultVersion) {
    _defaultVersion = defaultVersion;
  }

  public void setVersion(int version) {
    _version = version;
  }

  public void setIncludeReferences(boolean includeReferences) {
    _includeReferences = includeReferences;
  }

  public ResponseBean getModel() {
    return _response;
  }

  /****
   * Protected Methods
   * 
   * @param version
   * @return
   */

  protected boolean isVersion(int version) {
    if (_version == NO_VERSION)
      return version == _defaultVersion;
    else
      return version == _version;
  }

  protected BeanFactoryV2 getBeanFactoryV2() {
    return new BeanFactoryV2(_includeReferences);
  }

  protected BeanFactoryV2 getBeanFactoryV2(MaxCountSupport maxCount) {
    BeanFactoryV2 factory = getBeanFactoryV2();
    factory.setMaxCount(maxCount);
    return factory;
  }

  /*****************************************************************************
   * Response Bean Generation Methods
   ****************************************************************************/

  protected DefaultHttpHeaders setOkResponse(Object data) {
    _response = new ResponseBean(getReturnVersion(), ResponseCodes.RESPONSE_OK,
        "OK", data);
    return new DefaultHttpHeaders();
  }

  protected DefaultHttpHeaders setValidationErrorsResponse() {
    ValidationErrorBean bean = new ValidationErrorBean(new ArrayList<String>(
        getActionErrors()), getFieldErrors());
    _response = new ResponseBean(getReturnVersion(),
        ResponseCodes.RESPONSE_INVALID_ARGUMENT, "validation error", bean);
    return new DefaultHttpHeaders().withStatus(_response.getCode());
  }

  protected DefaultHttpHeaders setResourceNotFoundResponse() {
    _response = new ResponseBean(getReturnVersion(),
        ResponseCodes.RESPONSE_RESOURCE_NOT_FOUND, "resource not found", null);
    return new DefaultHttpHeaders().withStatus(_response.getCode());
  }

  protected DefaultHttpHeaders setExceptionResponse() {
    _response = new ResponseBean(getReturnVersion(),
        ResponseCodes.RESPONSE_SERVICE_EXCEPTION, "internal error", null);
    return new DefaultHttpHeaders().withStatus(_response.getCode());
  }

  protected DefaultHttpHeaders setUnknownVersionResponse() {
    _response = new ResponseBean(getReturnVersion(),
        ResponseCodes.RESPONSE_SERVICE_EXCEPTION, "unknown version: "
            + _version, null);
    return new DefaultHttpHeaders().withStatus(_response.getCode());
  }

  protected int getReturnVersion() {
    if (_version == NO_VERSION)
      return _defaultVersion;
    return _version;
  }

}
