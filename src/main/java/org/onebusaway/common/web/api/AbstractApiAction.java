package org.onebusaway.common.web.api;

import com.opensymphony.xwork2.ActionSupport;

import org.onebusaway.common.web.common.client.rpc.ServiceException;

public abstract class AbstractApiAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  private static final String TYPE_JSON = "json";

  private String _version;

  private String _output;

  private ResponseBean _response;

  public AbstractApiAction(String version) {
    _version = version;
  }

  public void setOutput(String output) {
    _output = output;
  }

  public ResponseBean getResponse() {
    return _response;
  }

  @Override
  public String execute() throws Exception {

    String outputType = TYPE_JSON;

    if (_output == null || TYPE_JSON.equals(_output)) {
      outputType = TYPE_JSON;
    } else {
      _response = getInvalidArgumentResponse("output");
    }

    if (_response == null)
      _response = executeWithResponse();

    return outputType;
  }

  protected abstract ResponseBean executeWithResponse();

  /*****************************************************************************
   * Response Bean Generation Methods
   ****************************************************************************/

  protected ResponseBean getOkResponse(Object data) {
    return new ResponseBean(_version, ResponseCodes.RESPONSE_OK, "OK", data);
  }

  protected ResponseBean getInvalidArgumentResponse(String argumentName) {
    return new ResponseBean(_version, ResponseCodes.RESPONSE_INVALID_ARGUMENT,
        "invalid argument: " + argumentName, null);
  }

  protected ResponseBean getServiceExceptionResponse(ServiceException ex) {
    return new ResponseBean(_version, ResponseCodes.RESPONSE_SERVICE_EXCEPTION,
        ex.getMessage(), null);
  }

}
