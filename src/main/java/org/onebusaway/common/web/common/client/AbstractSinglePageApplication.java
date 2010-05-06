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
package org.onebusaway.common.web.common.client;

import org.onebusaway.common.web.common.client.context.Context;
import org.onebusaway.common.web.common.client.context.ContextImpl;
import org.onebusaway.common.web.common.client.context.ContextListener;
import org.onebusaway.common.web.common.client.context.ContextManager;
import org.onebusaway.common.web.common.client.context.HistoryContextManager;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.HistoryListener;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.HashMap;
import java.util.Map;

public class AbstractSinglePageApplication implements EntryPoint, ContextListener, ExceptionListener {

  private ContextManager _contextManager = new HistoryContextManager();

  /*****************************************************************************
   * Private Members
   ****************************************************************************/

  private IPageSource _pageSource;

  private Widget _pageWidget = null;

  private IPageSource _exceptionPage = new ExceptionPage();

  private boolean _hadError = false;

  private RootPanel _panel;

  /*****************************************************************************
   * Public Methods
   ****************************************************************************/

  public void handleException(Throwable ex) {

  }
  
  public ContextManager getContextManager() {
    return _contextManager;
  }

  /*****************************************************************************
   * {@link EntryPoint}
   ****************************************************************************/

  public void onModuleLoad() {

    System.out.println("onModuleLoad: AbstractSinglePageApplication");

    ExceptionSupport.addExceptionListener(this);

    _panel = RootPanel.get("content");

    _contextManager.addContextListener(this);

    Context context = _contextManager.getContext();

    if (context == null)
      context = new ContextImpl();
    onContextChanged(context);
  }

  /*****************************************************************************
   * {@link HistoryListener} Interface
   ****************************************************************************/

  public void onContextChanged(Context context) {
    try {

      if (_pageWidget == null || _hadError) {
        _panel.clear();
        _pageWidget = _pageSource.create(context);
        _panel.add(_pageWidget);
      } else {
        Widget widget = _pageSource.update(context);
        if (widget != null) {
          _panel.clear();
          _pageWidget = widget;
          _panel.add(_pageWidget);
        }
      }

    } catch (PageException ex) {
      onException(ex);
    }
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
   * Private Methods
   ****************************************************************************/

  protected void setPage(IPageSource page) {
    _pageSource = page;
  }

  protected void setExceptionPage(IPageSource exceptionPage) {
    _exceptionPage = exceptionPage;
  }
}
