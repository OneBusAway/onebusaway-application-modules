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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.onebusaway.webapp.gwt.common.widgets.SpanWidget;
import org.onebusaway.webapp.gwt.viewkit.events.ViewControllerSelectedEvent;
import org.onebusaway.webapp.gwt.viewkit.events.ViewControllerSelectedHandler;
import org.onebusaway.webapp.gwt.viewkit.resources.ViewKitCssResource;
import org.onebusaway.webapp.gwt.viewkit.resources.ViewKitResources;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

public class TabBarController extends ViewController {

  private static final ViewKitCssResource _css = ViewKitResources.INSTANCE.getCSS();

  private List<ViewController> _viewControllers = new ArrayList<ViewController>();

  private Grid _panel = new Grid(2, 1);

  private HorizontalPanel _tabPanel = new HorizontalPanel();

  private int _selectedIndex = -1;

  private Widget _selectedView = null;

  private boolean _visible = false;

  private HandlerManager _handlerManager;

  public void addVieControllerSelectedHandler(
      ViewControllerSelectedHandler handler) {
    ensureHandlers().addHandler(ViewControllerSelectedEvent.TYPE, handler);
  }

  public void addViewController(ViewController viewController) {

    // Does this really make sense?
    viewController.setNavigationController(getNavigationController());
    
    _viewControllers.add(viewController);
    addTabForViewController(viewController, _viewControllers.size() - 1);
    if (_selectedIndex == -1)
      setSelectedIndex(_viewControllers.size() - 1);
  }

  public List<ViewController> getViewControllers() {
    return Collections.unmodifiableList(_viewControllers);
  }

  public boolean hasSelectedViewController() {
    return _selectedIndex != -1;
  }

  public ViewController getSelectedViewController() {
    if (!hasSelectedViewController())
      return null;
    return _viewControllers.get(_selectedIndex);
  }

  public void setSelectedViewController(ViewController controller) {
    int index = _viewControllers.indexOf(controller);
    if (index != -1)
      setSelectedIndex(index);
  }

  public void setSelectedIndex(int index) {

    if (index == _selectedIndex)
      return;

    if (_selectedIndex != -1) {
      ViewController vc = _viewControllers.get(_selectedIndex);
      if (_visible)
        vc.viewWillDisappear();
      _panel.setText(0, 0, "");
      _selectedView.removeStyleName(_css.TabBarControllerContent());
      if (_visible)
        vc.viewDidDisappear();
    }

    ViewController viewController = _viewControllers.get(index);
    _selectedIndex = index;
    _selectedView = viewController.getView();

    if (_visible)
      viewController.viewWillAppear();

    _panel.setWidget(0, 0, _selectedView);
    _selectedView.addStyleName(_css.TabBarControllerContent());

    ensureHandlers().fireEvent(
        new ViewControllerSelectedEvent(viewController, _selectedIndex));

    if (_visible)
      viewController.viewDidAppear();
    
    fireContextChangedEvent();
  }

  /****
   * {@link ViewController} Methods
   ****/

  @Override
  public void viewWillAppear() {
    super.viewWillAppear();

    if (hasSelectedViewController())
      getSelectedViewController().viewWillAppear();
  }

  @Override
  public void viewDidAppear() {
    super.viewDidAppear();

    _visible = true;

    if (hasSelectedViewController())
      getSelectedViewController().viewDidAppear();
  }

  @Override
  public void viewWillDisappear() {
    super.viewWillDisappear();

    if (hasSelectedViewController())
      getSelectedViewController().viewWillDisappear();
  }

  @Override
  public void viewDidDisappear() {
    super.viewDidDisappear();

    _visible = false;

    if (hasSelectedViewController())
      getSelectedViewController().viewDidDisappear();
  }

  @Override
  protected void loadView() {
    super.loadView();

    _panel.addStyleName(_css.TabBarController());
    _tabPanel.addStyleName(_css.TabBar());

    _panel.setWidget(1, 0, _tabPanel);
    _panel.getCellFormatter().addStyleName(1, 0, _css.TabBarControllerTabBar());

    _view = _panel;
  }

  @Override
  public void setNavigationController(NavigationController controller) {
    
    super.setNavigationController(controller);

    for( ViewController views : _viewControllers)
      views.setNavigationController(controller);
  }

  @Override
  public void handleContext(List<String> path,
      Map<String, String> context) {
    
    if( path.isEmpty() )
      return;
    
    String itemId = path.remove(0);

    for (ViewController viewController : _viewControllers) {
      NavigationItem navItem = viewController.getNavigationItem();
      if (itemId.equals(navItem.getId())) {
        setSelectedViewController(viewController);
        viewController.handleContext(path,context);
        break;
      }
    }
  }
  
  @Override
  public void retrieveContext(List<String> path, Map<String, String> context) {
    if( _selectedIndex != -1 ) {
      ViewController vc = _viewControllers.get(_selectedIndex);
      NavigationItem item = vc.getNavigationItem();
      path.add(item.getId());
      vc.retrieveContext(path, context);
    }
  }

  /****
   * Private Methods
   ****/

  private void addTabForViewController(ViewController viewController,
      final int index) {

    TabBarItem tabBarItem = viewController.getTabBarItem();

    ClickableFlowPanel tab = new ClickableFlowPanel();
    tab.addStyleName(_css.TabBarItem());

    FlowPanel imagePart = new FlowPanel();
    tab.add(imagePart);

    Image image = tabBarItem.getImage();
    if (image != null) {
      imagePart.add(image);
    } else {
      imagePart.addStyleName(_css.TabBarItemNoImage());
    }

    FlowPanel namePart = new FlowPanel();
    tab.add(namePart);

    String name = tabBarItem.getName();
    if (name != null) {
      namePart.add(new SpanWidget(name));
    } else {
      namePart.addStyleName(_css.TabBarItemNoName());
    }

    tab.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent arg0) {
        setSelectedIndex(index);
      }
    });

    _tabPanel.add(tab);
  }

  private HandlerManager ensureHandlers() {
    return _handlerManager == null ? _handlerManager = new HandlerManager(this)
        : _handlerManager;
  }
}
