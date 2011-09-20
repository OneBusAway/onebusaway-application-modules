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
package org.onebusaway.webapp.gwt.common;

import org.onebusaway.webapp.gwt.common.context.Context;
import org.onebusaway.webapp.gwt.common.context.ContextImpl;
import org.onebusaway.webapp.gwt.common.context.ContextListener;
import org.onebusaway.webapp.gwt.common.context.ContextManager;
import org.onebusaway.webapp.gwt.common.context.HistoryContextManager;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.HashMap;
import java.util.Map;

public class AbstractMultiPageApplication implements EntryPoint, ContextListener, ExceptionListener {

  private static final String DEFAULT_PAGE_KEY = "p";

  private static AbstractMultiPageApplication _app;

  /*****************************************************************************
   * Private Members
   ****************************************************************************/

  private ContextManager _contextManager = new HistoryContextManager();

  private Map<String, IPageSource> _pages = new HashMap<String, IPageSource>();

  private FlowPanel _panel;

  private String _activePage = null;

  private IPageSource _exceptionPage = new ExceptionPage();

  private boolean _hadError;

  /*****************************************************************************
   * Static Methods
   ****************************************************************************/

  public static AbstractMultiPageApplication getApp() {
    return _app;
  }

  /*****************************************************************************
   * {@link ExceptionListener} Interface
   ****************************************************************************/

  public void onException(Throwable ex) {

    ex.printStackTrace();

    String msg = ex.getMessage();

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
    
    System.out.println("load=" + getClass());

    _app = this;

    RootPanel panel = RootPanel.get("content");

    _panel = new FlowPanel();

    panel.add(_panel);

    _contextManager.addContextListener(this);

    Context context = _contextManager.getContext();

    if (context == null)
      context = new ContextImpl();
    onContextChanged(context);
  }

  /*****************************************************************************
   * {@link ContextListener} Interface
   ****************************************************************************/

  public void onContextChanged(Context context) {

    try {

      CommonLibraryMessages c = CommonLibraryMessages.MESSAGES;

      String pageKey = context.getParam(DEFAULT_PAGE_KEY);

      if (pageKey == null)
        pageKey = "index";

      IPageSource pageFactory = _pages.get(pageKey);

      if (pageFactory == null)
        throw new PageException(c.invalidPage(pageKey));

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
      onException(ex);
    }
  }

  /*****************************************************************************
   * Private Methods
   ****************************************************************************/

  protected void addPage(String key, IPageSource page) {
    _pages.put(key, page);
  }

  protected void setExceptionPage(IPageSource exceptionPage) {
    _exceptionPage = exceptionPage;
  }

}
