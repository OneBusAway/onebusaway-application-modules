/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
