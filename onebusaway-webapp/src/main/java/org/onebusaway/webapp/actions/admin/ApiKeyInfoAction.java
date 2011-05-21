package org.onebusaway.webapp.actions.admin;

import java.util.Map;

import org.onebusaway.users.services.validation.KeyValidationService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;

public class ApiKeyInfoAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  @Autowired
  private KeyValidationService _validationService;

  private String _value;

  private Map<String, String> _info;

  @RequiredFieldValidator
  public void setValue(String value) {
    _value = value;
  }
  
  public Map<String, String> getInfo() {
    return _info;
  }

  @Override
  public String execute() {
    _info = _validationService.getKeyInfo(_value);
    return SUCCESS;
  }
}
