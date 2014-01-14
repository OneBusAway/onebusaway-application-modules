/**
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.webapp.actions;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionSupport;

public class ResourceNotFoundAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  private int _status = HttpServletResponse.SC_NOT_FOUND;

  public void setStatus(int status) {
    _status = status;
  }

  @Override
  public String execute() {
    HttpServletResponse response = ServletActionContext.getResponse();
    response.setStatus(_status);
    return SUCCESS;
  }
}
