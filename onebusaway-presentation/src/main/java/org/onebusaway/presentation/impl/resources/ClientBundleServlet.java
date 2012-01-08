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
package org.onebusaway.presentation.impl.resources;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ClientBundleServlet extends HttpServlet {

  private static final String INIT_PARAM_RESOURCE = "resource:";

  private static final String INIT_PARAM_PREFIX = "prefix";

  private static final long serialVersionUID = 1L;

  private ClientBundleFactory _factory = new ClientBundleFactory();

  /*****************************************************************************
   * {@link HttpServlet} Interface
   ****************************************************************************/

  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    loadResources(config);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    String remoteURL = req.getServletPath() + req.getPathInfo();
    LocalResource resource = _factory.getResourceForExternalUrl(remoteURL);

    if (resource != null)
      writeResponseFromResource(resp, resource);
    else
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
  }

  /*****************************************************************************
   * Private Methods
   ****************************************************************************/

  private void loadResources(ServletConfig config) {

    ServletContext servletContext = config.getServletContext();
    _factory.setServletContext(servletContext);

    for (Enumeration<?> en = config.getInitParameterNames(); en.hasMoreElements();) {

      String name = (String) en.nextElement();
      String value = config.getInitParameter(name);

      if (INIT_PARAM_PREFIX.equals(name)) {
        _factory.setPrefix(value);
      } else if (name.startsWith(INIT_PARAM_RESOURCE)) {
        String resourceName = name.substring(INIT_PARAM_RESOURCE.length());
        try {
          Class<?> resourceType = Class.forName(value);
          _factory.addResource(resourceType);
        } catch (ClassNotFoundException ex) {
          servletContext.log("error loading resource name=" + resourceName
              + " type=" + value, ex);
        } catch (IOException ex) {
          servletContext.log("error creating resource name=" + resourceName
              + " type=" + value, ex);
        }
      }
    }

    servletContext.setAttribute("resources", _factory.getBundles());
  }

  private void writeResponseFromResource(HttpServletResponse resp,
      LocalResource resource) throws IOException {

    resp.setDateHeader("Last-Modified", resource.getLastModifiedTime());

    ServletOutputStream out = resp.getOutputStream();
    URL localUrl = resource.getLocalUrl();
    InputStream in = localUrl.openStream();

    byte[] buffer = new byte[1024];
    while (true) {
      int rc = in.read(buffer);

      if (rc == -1)
        break;
      out.write(buffer, 0, rc);
    }

    in.close();
    out.close();
  }
}
