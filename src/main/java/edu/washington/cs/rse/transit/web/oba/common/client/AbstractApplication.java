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
package edu.washington.cs.rse.transit.web.oba.common.client;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.HistoryListener;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.washington.cs.rse.transit.web.oba.common.client.rpc.InvalidSelectionServiceException;
import edu.washington.cs.rse.transit.web.oba.common.client.rpc.NoSuchRouteServiceException;
import edu.washington.cs.rse.transit.web.oba.common.client.rpc.NoSuchServicePatternServiceException;
import edu.washington.cs.rse.transit.web.oba.common.client.rpc.NoSuchStopServiceException;
import edu.washington.cs.rse.transit.web.oba.common.client.rpc.OneBusAwayServiceAsync;
import edu.washington.cs.rse.transit.web.oba.common.client.url.ParaUrlCodingStrategy;
import edu.washington.cs.rse.transit.web.oba.common.client.url.UrlCodingStrategy;

public class AbstractApplication implements EntryPoint, HistoryListener {

  private static final String DEFAULT_PAGE_KEY = "p";

  private static AbstractApplication _app;

  private static UrlCodingStrategy _codingStrategy = new ParaUrlCodingStrategy();

  /*****************************************************************************
   * Private Members
   ****************************************************************************/

  private Map<String, IPageSource> _pages = new HashMap<String, IPageSource>();

  private FlowPanel _panel;

  private String _activePage = null;

  private IPageSource _exceptionPage = new ExceptionPage();

  private boolean _hadError;

  /*****************************************************************************
   * Static Methods
   ****************************************************************************/

  public static AbstractApplication getApp() {
    return _app;
  }

  /*****************************************************************************
   * Public Methods
   ****************************************************************************/

  public String getTargetWithMap(String target, Map<String, String> params) {
    Map<String, String> p = new LinkedHashMap<String, String>();
    p.put(DEFAULT_PAGE_KEY, target);
    p.putAll(params);
    return _codingStrategy.getParamMapAsString(p);
  }

  public void handleException(Throwable ex) {

    ex.printStackTrace();

    OneBusAwayMessages msgs = OneBusAwayCommon.MESSAGES;

    String msg = ex.getMessage();

    if (ex instanceof NoSuchRouteServiceException)
      msg = msgs.commonNoSuchRoute();
    else if (ex instanceof NoSuchStopServiceException)
      msg = msgs.commonNoSuchStop();
    else if (ex instanceof NoSuchServicePatternServiceException)
      msg = msgs.commonNoSuchServicePattern();
    else if (ex instanceof InvalidSelectionServiceException)
      msg = msgs.commonInvalidSelection();

    Map<String, String> p = new HashMap<String, String>();
    p.put("message", msg);
    ContextImpl c = new ContextImpl(p);

    try {
      _panel.clear();
      Widget widget = _exceptionPage.create(c);
      _panel.add(widget);
      _hadError = true;
    } catch (PageException e) {
      e.printStackTrace();
    }
  }

  /*****************************************************************************
   * {@link EntryPoint}
   ****************************************************************************/

  public void onModuleLoad() {

    _app = this;

    String href = getServiceEntryPoint();
    ServiceDefTarget target = (ServiceDefTarget) OneBusAwayServiceAsync.SERVICE;
    target.setServiceEntryPoint(href);

    RootPanel panel = RootPanel.get("content");

    _panel = new FlowPanel();

    panel.add(_panel);

    History.addHistoryListener(this);

    String token = History.getToken();

    if (token == null || token.length() == 0) {
      token = getTargetWithMap("index", new HashMap<String, String>());
      History.newItem(token);
    } else {
      onHistoryChanged(token);
    }

  }

  /*****************************************************************************
   * {@link HistoryListener} Interface
   ****************************************************************************/

  public void onHistoryChanged(String token) {

    try {

      OneBusAwayMessages c = OneBusAwayCommon.MESSAGES;

      Map<String, String> m = _codingStrategy.getParamStringAsMap(token);
      String pageKey = m.get(DEFAULT_PAGE_KEY);

      if (pageKey == null)
        pageKey = "index";

      IPageSource pageFactory = _pages.get(pageKey);

      if (pageFactory == null)
        throw new PageException(c.commonInvalidPage(pageKey));

      ContextImpl context = new ContextImpl(m);

      if (_activePage == null || !_activePage.equals(pageKey) || _hadError) {
        _panel.clear();
        _hadError = false;
        _panel.add(pageFactory.create(context));
      } else {
        Widget widget = pageFactory.update(context);
        if (widget != null) {
          _hadError = false;
          _panel.clear();
          _panel.add(widget);
        }
      }

      _activePage = pageKey;

    } catch (PageException ex) {
      Window.alert("had exception");
      handleException(ex);
    }
  }

  /*****************************************************************************
   * Private Methods
   ****************************************************************************/

  private String getServiceEntryPoint() {
    if (Location.getHost().equals("localhost:8888"))
      return "http://localhost:8888/edu.washington.cs.rse.transit.web.oba.standard.OneBusAwayStandardApplication/onebusaway";

    String path = Location.getPath();
    if (path.startsWith("/edu.washington.cs.rse.transit"))
      return Location.getProtocol() + "//" + Location.getHost()
          + "/edu.washington.cs.rse.transit/onebusaway";

    return Location.getProtocol() + "//" + Location.getHost() + "/onebusaway";
  }

  protected void addPage(String key, IPageSource page) {
    _pages.put(key, page);
  }

  protected void setExceptionPage(IPageSource exceptionPage) {
    _exceptionPage = exceptionPage;
  }

  /*****************************************************************************
   * 
   ****************************************************************************/

  private class ContextImpl implements Context {

    private Map<String, String> _params;

    public ContextImpl(Map<String, String> params) {
      _params = params;
    }

    public boolean hasParam(String name) {
      return _params.containsKey(name);
    }

    public String getParam(String name) {
      return _params.get(name);
    }

    public Map<String, String> getParams() {
      return _params;
    }
  }

}
