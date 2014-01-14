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
package org.onebusaway.presentation.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;

public class ProxyServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  private String _target;

  private String _source;

  @Override
  public void init(ServletConfig config) throws ServletException {
    _target = config.getInitParameter("target");
    if (_target == null)
      throw new ServletException("you did not specify a target parameter");

    _source = config.getInitParameter("source");
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    GetMethod method = new GetMethod(proxyUrl(req));
    executeMethod(method, resp);
  }

  /****
   * Private Method
   ****/

  private String proxyUrl(HttpServletRequest req) {

    String pathInfo = req.getRequestURI();
    
    if (_source != null)
      pathInfo = pathInfo.replaceFirst(_source, "");

    String url = _target + pathInfo;

    if (!_target.startsWith("http"))
      url = "http://" + req.getLocalName() + ":" + req.getLocalPort() + url;

    if (req.getQueryString() != null)
      url += "?" + req.getQueryString();
    return url;
  }

  private void executeMethod(HttpMethod method, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpClient client = new HttpClient();

    int status = client.executeMethod(method);

    resp.setStatus(status);

    // Pass response headers back to the client
    Header[] headerArrayResponse = method.getResponseHeaders();
    for (Header header : headerArrayResponse)
      resp.setHeader(header.getName(), header.getValue());

    // Send the content to the client
    InputStream in = method.getResponseBodyAsStream();
    OutputStream out = resp.getOutputStream();

    byte[] buffer = new byte[1024];
    int rc;
    while ((rc = in.read(buffer)) != -1)
      out.write(buffer, 0, rc);
  }
}
