/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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
package org.onebusaway.api.actions.api.where.search;

import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base class for search actions.
 */
public class ApiSearchAction extends ApiActionSupport {

  protected static final int V2 = 2;

  protected static final long serialVersionUID = 1L;

  protected String _input;

  protected int maxCount = 20;
  @Autowired
  protected TransitDataService _service;

  @RequiredFieldValidator(message = "missing input")
  public void setInput(String input) {
    if (input != null)
      _input = input.toLowerCase();
  }

  public void setMaxCount(int max) {
    this.maxCount = max;
  }

  public String getInput() {
    return _input;
  }


  public ApiSearchAction(int version) {
    super(version);
  }
}
