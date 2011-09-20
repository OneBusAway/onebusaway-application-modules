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

import org.onebusaway.webapp.gwt.viewkit.BarButtonItem.EBarButtonSystemItem;
import org.onebusaway.webapp.gwt.viewkit.resources.ViewKitCssResource;
import org.onebusaway.webapp.gwt.viewkit.resources.ViewKitResources;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

public class NavigationBar {

  private static final ViewKitCssResource _css = ViewKitResources.INSTANCE.getCSS();

  private Grid _panel;

  private List<NavigationItem> _items = new ArrayList<NavigationItem>();

  private NavigationController _controller;

  public NavigationBar(NavigationController controller) {
    _controller = controller;
  }

  public Widget getView() {

    if (_panel == null) {

      _panel = new Grid(1, 3);
      _panel.setCellPadding(0);
      _panel.setCellSpacing(0);

      _panel.addStyleName(_css.NavigationBar());
      _panel.getCellFormatter().addStyleName(0, 0, _css.NavigationBarLeftItem());
      _panel.getCellFormatter().setAlignment(0, 0,
          HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE);

      _panel.getCellFormatter().addStyleName(0, 1,
          _css.NavigationBarCenterItem());
      _panel.getCellFormatter().setAlignment(0, 1,
          HasHorizontalAlignment.ALIGN_CENTER,
          HasVerticalAlignment.ALIGN_MIDDLE);

      _panel.getCellFormatter().addStyleName(0, 2,
          _css.NavigationBarRightItem());
      _panel.getCellFormatter().setAlignment(0, 2,
          HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE);
    }

    return _panel;
  }

  public void pushNavigationItem(NavigationItem item) {
    _items.add(item);
    refresh();
  }

  public void popNavigationItem() {
    if (!_items.isEmpty()) {
      _items.remove(_items.size() - 1);
      refresh();
    }
  }

  public void popToRootNavigationItem() {
    while( _items.size() > 1 )
      _items.remove(_items.size() - 1);
    refresh();
  }

  private void refresh() {

    getView();

    _panel.setText(0, 0, "");
    _panel.setText(0, 1, "");
    _panel.setText(0, 2, "");

    if (_items.isEmpty())
      return;

    NavigationItem item = _items.get(_items.size() - 1);

    String title = item.getTitle();
    if (title != null) {
      _panel.setText(0, 1, title);
    }

    if (item.getLeftBarButtonItem() != null) {
      addBarButtonItemToBar(item.getLeftBarButtonItem(), 0);
    } else {
      NavigationItem back = null;
      if (_items.size() > 1)
        back = _items.get(_items.size() - 2);

      if (back != null) {
        String backTitle = "Back";
        if (back.getTitle() != null)
          backTitle = back.getTitle();

        Grid backButton = createBackButton(backTitle);

        backButton.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent arg0) {
            _controller.popViewController();
          }
        });
        _panel.setWidget(0, 0, backButton);
      }
    }

    if (item.getRightBarButtonItem() != null)
      addBarButtonItemToBar(item.getRightBarButtonItem(), 2);
  }

  private void addBarButtonItemToBar(BarButtonItem item, int index) {
    Grid button = null;
    if (item.getSystemItem() != null)
      button = createButton(item.getSystemItem());
    else if (item.getTitle() != null)
      button = createButton(item.getTitle());
    if (button != null) {
      _panel.setWidget(0, index, button);
      if (item.getClickHandler() != null)
        button.addClickHandler(item.getClickHandler());
    }
  }

  private Grid createBackButton(String text) {
    Grid backPanel = new Grid(1, 3);
    backPanel.addStyleName(_css.NavigationBarButton());
    backPanel.getCellFormatter().addStyleName(0, 0,
        _css.NavigationBarButtonLeftArrow());
    backPanel.getCellFormatter().addStyleName(0, 1,
        _css.NavigationBarButtonCenter());
    backPanel.getCellFormatter().addStyleName(0, 2,
        _css.NavigationBarButtonRightRounded());
    backPanel.setText(0, 0, "");
    backPanel.setText(0, 1, text);
    backPanel.setText(0, 2, "");
    return backPanel;
  }

  private Grid createButton(String text) {
    Grid button = createButton();
    button.setText(0, 1, text);
    return button;
  }

  private Grid createButton(EBarButtonSystemItem systemItem) {
    ViewKitResources resources = ViewKitResources.INSTANCE;
    ImageResource r = null;
    switch (systemItem) {
      case CROSS_HAIRS:
        r = resources.BarButtonSystemCrossHairsImage();
        break;
      case REFRESH:
      default:
        r = resources.BarButtonSystemRefreshImage();
        break;
    }
    Image image = new Image(r.getURL(), r.getLeft(), r.getTop(), r.getWidth(),
        r.getHeight());
    Grid button = createButton();
    button.setWidget(0, 1, image);
    return button;
  }

  private Grid createButton() {
    Grid backPanel = new Grid(1, 3);
    backPanel.addStyleName(_css.NavigationBarButton());
    backPanel.getCellFormatter().addStyleName(0, 0,
        _css.NavigationBarButtonLeftRounded());
    backPanel.getCellFormatter().addStyleName(0, 1,
        _css.NavigationBarButtonCenter());
    backPanel.getCellFormatter().addStyleName(0, 2,
        _css.NavigationBarButtonRightRounded());
    backPanel.setText(0, 0, "");
    backPanel.setText(0, 2, "");
    return backPanel;
  }

}
