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
package org.onebusaway.webapp.gwt.viewkit;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.onebusaway.webapp.gwt.common.context.Context;
import org.onebusaway.webapp.gwt.common.context.ContextImpl;
import org.onebusaway.webapp.gwt.common.context.ContextListener;
import org.onebusaway.webapp.gwt.common.context.ContextManager;
import org.onebusaway.webapp.gwt.common.context.HistoryContextManager;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

public class AbstractApplication implements EntryPoint, ContextListener {

  private ContextManager _contextManager = new HistoryContextManager();
  
  private RootPanel _panel;
  
  private RootNavigationController _rootNavigationController = new RootNavigationController();

  /*****************************************************************************
   * Public Methods
   ****************************************************************************/

  public ContextManager getContextManager() {
    return _contextManager;
  }

  public void setContextManager(ContextManager contextManager) {
    _contextManager = contextManager;
  }
  
  /*****************************************************************************
   * {@link EntryPoint}
   ****************************************************************************/

  public void onModuleLoad() {

    _panel = RootPanel.get("content");

    Widget view = _rootNavigationController.getView();
    
    _rootNavigationController.viewWillAppear();
    _panel.add(view);
    _rootNavigationController.viewDidAppear();
    
    _contextManager.addContextListener(this);

    Context context = _contextManager.getContext();

    if (context == null)
      context = new ContextImpl();

    final Context finalContext = context;

    // We defer execution of the command so that other modules can finish
    // loading before we fire the context change event
    DeferredCommand.addCommand(new Command() {
      public void execute() {
        onContextChanged(finalContext);
      }
    });
  }
  
  public void onContextChanged(Context context) {
    if( context.hasParam("p") ) {
      String fullPath = context.getParam("p");
      List<String> path = new LinkedList<String>();
      for( String token : fullPath.split("/"))
        path.add(token);
      _rootNavigationController.handleContext(path, context.getParams());
      
    }
  }
  
  protected void setRootViewController(ViewController viewController) {
    _rootNavigationController.pushViewController(viewController);
  }
  
  /****
   * Private Methods
   ****/
  
  private class RootNavigationController extends NavigationController {

    @Override
    protected void fireContextChangedEvent() {
      
      List<String> path = new LinkedList<String>();
      Map<String, String> context = new HashMap<String, String>();
      retrieveContext(path, context);

      StringBuilder b = new StringBuilder();
      
      for( String token : path){
        if( b.length() > 0)
          b.append("/");
        b.append(token);
      }
      
      String fullPath = b.toString();
      context.put("p", fullPath);
      
      System.out.println("============================");
      System.out.println("context=" + context);
      
      _contextManager.setContext(new ContextImpl(context));
    }
  }
}
