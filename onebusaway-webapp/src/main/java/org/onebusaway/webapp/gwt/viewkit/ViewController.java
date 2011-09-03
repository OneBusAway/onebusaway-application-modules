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

import java.util.List;
import java.util.Map;

import org.onebusaway.webapp.gwt.common.widgets.DivWidget;
import org.onebusaway.webapp.gwt.viewkit.resources.ViewKitCssResource;
import org.onebusaway.webapp.gwt.viewkit.resources.ViewKitResources;

import com.google.gwt.user.client.ui.Widget;

public class ViewController implements ContextAware {

  private static ViewKitCssResource _css = ViewKitResources.INSTANCE.getCSS();

  private NavigationController _navigationController;

  private boolean _viewLoaded = false;

  protected Widget _view;

  private TabBarItem _tabBarItem = null;

  private NavigationItem _navigationItem = null;

  public NavigationController getNavigationController() {
    return _navigationController;
  }

  public void setNavigationController(NavigationController controller) {
    _navigationController = controller;
  }

  Widget getView() {
    if (!_viewLoaded) {
      loadView();
      _viewLoaded = true;
      viewDidLoad();
    }
    _view.addStyleName(_css.ViewController());
    return _view;
  }

  public TabBarItem getTabBarItem() {
    if (_tabBarItem == null)
      _tabBarItem = new TabBarItem();
    return _tabBarItem;
  }

  public NavigationItem getNavigationItem() {
    if (_navigationItem == null)
      _navigationItem = new NavigationItem();
    return _navigationItem;
  }

  public void viewDidLoad() {

  }

  public void viewDidUnload() {

  }

  public void viewWillAppear() {

  }

  public void viewDidAppear() {

  }

  public void viewWillDisappear() {

  }

  public void viewDidDisappear() {

  }

  public void handleContext(List<String> path, Map<String, String> context) {

  }
  
  public void retrieveContext(List<String> path, Map<String, String> context) {
    
  }

  /****
   * Protected Methods
   ****/
  
  protected void fireContextChangedEvent() {
    NavigationController controller = getNavigationController();
    if( controller != null)
      controller.fireContextChangedEvent();
  }

  protected void loadView() {
    if (_view == null)
      _view = new DivWidget("empty");
  }

}
