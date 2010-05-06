package org.onebusaway.webapp.actions.admin;

import org.onebusaway.users.services.validation.KeyValidationService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;

public class ApiKeyGeneratorAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  @Autowired
  private KeyValidationService _validationService;

  private String _value;

  private String _key;

  @RequiredFieldValidator
  public void setValue(String value) {
    _value = value;
  }
  
  public String getValue() {
    return _value;
  }

  public String getKey() {
    return _key;
  }

  @Override
  public String execute() {

    _key = _validationService.generateKey(_value);

    return SUCCESS;
  }
}
