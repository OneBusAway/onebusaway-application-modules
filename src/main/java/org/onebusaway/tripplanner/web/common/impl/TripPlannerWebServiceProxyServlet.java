package org.onebusaway.tripplanner.web.common.impl;

import org.onebusaway.common.web.gwt.ServiceProxyServlet;

import java.net.MalformedURLException;
import java.net.URL;

public class TripPlannerWebServiceProxyServlet extends ServiceProxyServlet {

  private static final long serialVersionUID = 1L;

  public TripPlannerWebServiceProxyServlet() {
    super(getUrl());
  }

  private static URL getUrl() {
    try {
      return new URL("http://localhost:8080/org.onebusaway/services/tripplanner");
    } catch (MalformedURLException e) {
      e.printStackTrace();
      throw new IllegalStateException("error parsing url", e);
    }
  }
}
