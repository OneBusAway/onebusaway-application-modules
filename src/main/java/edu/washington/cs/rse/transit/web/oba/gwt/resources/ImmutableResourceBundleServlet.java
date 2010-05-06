/*
 * Copyright 2008 Brian Ferris
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.washington.cs.rse.transit.web.oba.gwt.resources;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ImmutableResourceBundleServlet extends HttpServlet {

  private static final String INIT_PARAM_RESOURCE = "resource:";

  private static final String INIT_PARAM_PREFIX = "prefix";

  private static final long serialVersionUID = 1L;

  private ImmutableResourceBundleFactory _factory = new ImmutableResourceBundleFactory(
      new UrlStrategyImpl());

  private String _prefix = "";

  private boolean _developmentMode = false;

  private Map<String, URL> _remoteUrlToLocalUrl = new HashMap<String, URL>();

  private String _contextPath;

  public void setPrefix(String prefix) {
    _prefix = prefix;
  }

  public void setDevelopmentMode(String mode) {
    _developmentMode = Boolean.parseBoolean(mode);
  }

  public void setResources(Map<String, Class<?>> resources) throws IOException {
    for (Map.Entry<String, Class<?>> entry : resources.entrySet())
      addResource(entry.getKey(), entry.getValue());
  }

  public void addResource(String key, Class<?> bundleType) throws IOException {
    _factory.addResource(key, bundleType);
  }

  /*****************************************************************************
   * {@link HttpServlet} Interface
   ****************************************************************************/

  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
    context.getAutowireCapableBeanFactory().autowireBean(this);
    ServletContext servletContext = config.getServletContext();
    _contextPath = getContextPath(servletContext);
    loadResources(config);
    servletContext.setAttribute("resources", _factory.getBundles());
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    String remoteURL = req.getServletPath() + req.getPathInfo();
    URL localURL = _remoteUrlToLocalUrl.get(remoteURL);

    if (localURL != null)
      writeResponseFromURL(resp, localURL);
    else
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
  }

  /*****************************************************************************
   * Private Methods
   ****************************************************************************/

  private File getTempDir(ServletContext context) {
    File tmpDir = (File) context.getAttribute("javax.servlet.context.tempdir");
    if (tmpDir != null)
      return tmpDir;
    System.err.println("NO ServletContext TEMP DIR!");
    return new File(System.getProperty("java.io.tmpdir"));
  }

  private void loadResources(ServletConfig config) {

    ServletContext servletContext = config.getServletContext();
    File tempDir = getTempDir(servletContext);
    _factory.setTempDir(tempDir);

    for (Enumeration<?> en = config.getInitParameterNames(); en.hasMoreElements();) {

      String name = (String) en.nextElement();
      String value = config.getInitParameter(name);

      if (INIT_PARAM_PREFIX.equals(name)) {
        _prefix = value;
      } else if (name.startsWith(INIT_PARAM_RESOURCE)) {
        String resourceName = name.substring(INIT_PARAM_RESOURCE.length());
        try {
          Class<?> resourceType = Class.forName(value);
          addResource(resourceName, resourceType);
        } catch (ClassNotFoundException ex) {
          servletContext.log("error loading resource name=" + resourceName
              + " type=" + value, ex);
        } catch (IOException ex) {
          servletContext.log("error creating resource name=" + resourceName
              + " type=" + value, ex);
        }
      }
    }

    Map<String, ImmutableResourceBundle> resources = _factory.getBundles();
    for (ImmutableResourceBundle bundle : resources.values()) {
      for (ResourcePrototype prototype : bundle.getResources()) {
        if (prototype instanceof LocalResource) {
          LocalResource resource = (LocalResource) prototype;
          String remoteUrl = resource.getRemoteUrl();
          URL localUrl = resource.getLocalUrl();
          _remoteUrlToLocalUrl.put(remoteUrl, localUrl);
        }
      }
    }
  }

  private void writeResponseFromURL(HttpServletResponse resp, URL localURL)
      throws IOException {

    resp.setDateHeader("Last-Modified", System.currentTimeMillis());

    ServletOutputStream out = resp.getOutputStream();
    InputStream in = localURL.openStream();
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

  private String getContextPath(ServletContext context) {
    // Get the context path without the request.
    String contextPath = "";
    try {
      String path = context.getResource("/").getPath();
      contextPath = path.substring(0, path.lastIndexOf("/"));
      contextPath = contextPath.substring(contextPath.lastIndexOf("/"));
      if( contextPath.equals("/localhost"))
        contextPath = "";
    } catch (Exception e) {
      e.printStackTrace();
    }
    return contextPath;
  }

  private class UrlStrategyImpl implements URLStrategy {

    public String construct(String bundleName, String resourceName,
        String resourceKey, String resourceExtension) {

      StringBuilder b = new StringBuilder();

      if (_prefix != null && _prefix.length() > 0)
        b.append(_prefix);

      b.append('/');
      b.append(bundleName);
      b.append('/');
      if (resourceName.startsWith("get"))
        resourceName = resourceName.substring(3);
      b.append(resourceName);
      b.append('-');
      b.append(resourceKey);

      if (!_developmentMode)
        b.append(".cache");

      if (resourceExtension != null && resourceExtension.length() > 0)
        b.append('.').append(resourceExtension);

      return b.toString();
    }

    public String addContext(String url) {
      if (_contextPath != null && _contextPath.length() > 0)
        url = _contextPath + url;
      return url;
    }
  }
}
