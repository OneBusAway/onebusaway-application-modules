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

import java.io.File;
import java.io.IOException;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.webapp.services.TccParticipantRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;

@Results( {@Result(type = "redirectAction", name="redirect", params = {"actionName", "index"})})
public class LinkTccParticipantsAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  private String _data;

  private File _file;

  private TccParticipantRegistrationService _registrationService;

  @Autowired
  public void setRegistrationService(
      TccParticipantRegistrationService registrationService) {
    _registrationService = registrationService;
  }

  public void setData(String data) {
    _data = data;
  }

  public void setFile(File file) {
    _file = file;
  }

  @Override
  public String execute() {
    return SUCCESS;
  }

  public String submit() throws IOException {

    if (_data != null && _data.length() > 0)
      _registrationService.register(_data);

    if (_file != null)
      _registrationService.register(_file);

    return "redirect";
  }
}
