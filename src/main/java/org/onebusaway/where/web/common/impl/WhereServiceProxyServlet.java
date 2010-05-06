package org.onebusaway.where.web.common.impl;

import org.onebusaway.common.web.gwt.ServiceProxyServlet;

import java.net.MalformedURLException;
import java.net.URL;

public class WhereServiceProxyServlet extends ServiceProxyServlet {

  private static final long serialVersionUID = 1L;

  public WhereServiceProxyServlet() {
    super(getUrl());
  }

  private static URL getUrl() {
    try {
      return new URL("http://localhost:8080/org.onebusaway/services/where");
    } catch (MalformedURLException e) {
      throw new IllegalStateException("error parsing url", e);
    }
  }

}
