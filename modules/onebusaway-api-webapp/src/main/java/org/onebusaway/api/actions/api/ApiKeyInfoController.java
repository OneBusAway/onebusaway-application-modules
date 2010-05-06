package org.onebusaway.api.actions.api;

import org.onebusaway.api.services.ApiKeyValidationService;

import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@ApiKeyAuthorization(enabled = false)
public class ApiKeyInfoController extends ApiActionSupport {

  private static final long serialVersionUID = 1L;

  @Autowired
  private ApiKeyValidationService _validationService;

  private String _value;

  public ApiKeyInfoController() {
    super("1.0");
  }

  @RequiredFieldValidator
  public void setValue(String value) {
    _value = value;
  }

  public DefaultHttpHeaders index() {
    Map<String, String> info = _validationService.getKeyInfo(_value);
    return setOkResponse(info);
  }
}
