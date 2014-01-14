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
import java.util.List;
import java.util.Map;

import org.onebusaway.webapp.gwt.viewkit.resources.ViewKitCssResource;
import org.onebusaway.webapp.gwt.viewkit.resources.ViewKitResources;

import com.google.gwt.user.client.ui.Grid;

public class NavigationController extends ViewController {

  private static final ViewKitCssResource _css = ViewKitResources.INSTANCE.getCSS();

  private List<ViewController> _viewControllers = new ArrayList<ViewController>();

  private Grid _panel = new Grid(2, 1);

  private NavigationBar _navigationBar = new NavigationBar(this);

  private boolean _visible = false;

  public int getViewControllerCount() {
    return _viewControllers.size();
  }

  public void pushViewController(ViewController viewController) {

    // Force a view load, just in case
    viewController.getView();

    ViewController existingController = hasTop() ? peek() : null;

    viewController.setNavigationController(this);
    _viewControllers.add(viewController);

    _navigationBar.pushNavigationItem(viewController.getNavigationItem());
    swapView(existingController, viewController);
  }

  public void popToRootViewController() {

    if (_viewControllers.size() < 2)
      return;

    ViewController oldViewContoller = pop();

    while (_viewControllers.size() > 1)
      pop();

    ViewController newViewController = peek();

    _navigationBar.popToRootNavigationItem();
    swapView(oldViewContoller, newViewController);
  }
  

  public void popToViewController(ViewController controller) {
    
    ViewController existingController = hasTop() ? peek() : null;
    int index = _viewControllers.indexOf(controller);
    if( index == -1) {
      _viewControllers.clear();
      _viewControllers.add(controller);
    }
    else {
      while( index + 1 < _viewControllers.size())
        pop();
    }
    
    swapView(existingController, controller);
  }

  public void popViewController() {

    if (!hasTop())
      return;

    ViewController oldViewContoller = hasTop() ? pop() : null;
    ViewController newViewController = hasTop() ? peek() : null;

    _navigationBar.popNavigationItem();
    swapView(oldViewContoller, newViewController);
  }

  public ViewController getPreviousController(ViewController controller) {
    return getRelativeController(controller, 1);
  }
  
  public ViewController getNextController(ViewController controller) {
    return getRelativeController(controller, 1);
  }
  
  /****
   * {@link ViewController} Methods
   ****/

  @Override
  public void viewWillAppear() {
    super.viewWillAppear();

    if (hasTop())
      peek().viewWillAppear();
  }

  @Override
  public void viewDidAppear() {
    super.viewDidAppear();

    _visible = true;

    if (hasTop())
      peek().viewDidAppear();
  }

  @Override
  public void viewWillDisappear() {
    super.viewWillDisappear();

    if (hasTop())
      peek().viewWillDisappear();
  }

  @Override
  public void viewDidDisappear() {
    super.viewDidDisappear();

    _visible = false;

    if (hasTop())
      peek().viewDidDisappear();
  }

  @Override
  protected void loadView() {
    super.loadView();

    _panel.addStyleName(_css.NavigationController());

    _panel.setWidget(0, 0, _navigationBar.getView());
    _panel.getCellFormatter().addStyleName(0, 0,
        _css.NavigationControllerNavigationBar());

    _view = _panel;
  }
  

  @Override
  protected void fireContextChangedEvent() {
    
    NavigationController parent = getNavigationController();
    
    if (parent != null)
      parent.fireContextChangedEvent();
  }
  
  @Override
  public void handleContext(List<String> path, Map<String, String> context) {
    if( _viewControllers.isEmpty() )
      return;
    
    ViewController root = _viewControllers.get(0);
    root.handleContext(path, context);
  }
  
  @Override
  public void retrieveContext(List<String> path, Map<String, String> context) {
    
    if( _viewControllers.isEmpty() )
      return;
    
    ViewController root = _viewControllers.get(0);
    root.retrieveContext(path, context);
  }

  /****
   * Private Methods
   ****/
  
  private ViewController getRelativeController(ViewController controller, int offset) {
    int index = _viewControllers.indexOf(controller);
    if( index == -1)
      return null;
    index += offset;
    if( index < 0 || _viewControllers.size() <= index)
      return null;
    return _viewControllers.get(index);
  }

  private void swapView(ViewController from, ViewController to) {
    
    if( from != null && from.equals(to))
      return;

    if (_visible) {
      if (from != null)
        from.viewWillDisappear();

      if (to != null)
        to.viewWillAppear();
    }

    if (to != null) {
      _panel.setWidget(1, 0, to.getView());
    } else {
      _panel.setText(1, 0, "");
    }

    if (_visible) {
      if (from != null)
        from.viewDidDisappear();
      if (to != null)
        to.viewDidAppear();
    }
    
    fireContextChangedEvent();
  }

  private boolean hasTop() {
    return !_viewControllers.isEmpty();
  }

  private ViewController peek() {
    return _viewControllers.get(_viewControllers.size() - 1);
  }

  private ViewController pop() {
    return _viewControllers.remove(_viewControllers.size() - 1);
  }

}
