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
package org.onebusaway.webapp.actions.user;

import java.io.InputStream;
import java.util.UUID;

import javax.servlet.ServletContext;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.util.ServletContextAware;
import org.onebusaway.users.model.IndexedUserDetails;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.services.CurrentUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;

@Results(value = {@Result(type = "stream", params = {
    "contentType", "contentType"})})
public class TccStudyRegistrationAction extends ActionSupport implements
    ServletContextAware {

  private static Logger _log = LoggerFactory.getLogger(TccStudyRegistrationAction.class);

  private static final long serialVersionUID = 1L;

  private CurrentUserService _currentUserService;

  private ServletContext _context;

  private String _id;

  private InputStream _inputStream;

  @Autowired
  public void setCurrentUserService(CurrentUserService currentUserService) {
    _currentUserService = currentUserService;
  }

  @Override
  public void setServletContext(ServletContext context) {
    _context = context;
  }

  public void setId(String id) {
    _id = id;
  }

  public String getContentType() {
    return "image/png";
  }

  public InputStream getInputStream() {
    return _inputStream;
  }

  @Override
  public String execute() {
    try {
      IndexedUserDetails details = _currentUserService.getCurrentUserDetails();
      String id = null;
      if (details == null) {
        id = UUID.randomUUID().toString();
      } else {
        UserIndexKey key = details.getUserIndexKey();
        id = key.getType() + "|" + key.getValue();
      }
      _currentUserService.getCurrentUser(true);
      _currentUserService.handleAddAccount("tccStudyId", id, _id, false);
    } catch (Exception ex) {
      _log.warn("error registering tcc study id", ex);
    }

    _inputStream = _context.getResourceAsStream("/WEB-INF/images/transparent.png");

    return SUCCESS;
  }

}
