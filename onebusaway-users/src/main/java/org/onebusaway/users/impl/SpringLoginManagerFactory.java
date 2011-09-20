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
package org.onebusaway.users.impl;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.onebusaway.everylastlogin.server.LoginManager;
import org.onebusaway.everylastlogin.server.LoginManagerFactory;
import org.onebusaway.everylastlogin.server.LoginServlet;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Factory class to create a {@link LoginManager} instance from an existing
 * Spring {@link WebApplicationContext} for use in the {@link LoginServlet}.
 * Assumes the presence of a {@link LoginManager} bean in the application
 * context with the name {@code "loginManager"}.
 * 
 * @author bdferris
 */
public class SpringLoginManagerFactory implements LoginManagerFactory {

  @Override
  public LoginManager createLoginManager(ServletConfig config)
      throws ServletException {

    ServletContext servletContext = config.getServletContext();
    WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
    return (LoginManager) webApplicationContext.getBean("loginManager");
  }

}
