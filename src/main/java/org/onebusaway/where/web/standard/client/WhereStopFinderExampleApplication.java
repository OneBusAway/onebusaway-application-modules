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
package org.onebusaway.where.web.standard.client;

import org.onebusaway.common.web.common.client.context.ContextManager;
import org.onebusaway.common.web.common.client.context.DirectContextManager;
import org.onebusaway.common.web.common.client.model.StopBean;
import org.onebusaway.common.web.common.client.widgets.DivPanel;
import org.onebusaway.common.web.common.client.widgets.DivWidget;
import org.onebusaway.where.web.common.client.view.StopFinderPresenter;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

public class WhereStopFinderExampleApplication implements EntryPoint {

  private ContextManager _contextManager = new DirectContextManager();

  private ListBox _stopList;

  public void onModuleLoad() {

    RootPanel root = RootPanel.get("content");
    final FlowPanel panel = new FlowPanel();
    panel.addStyleName("MyTestPanel");

    root.add(panel);

    DivWidget label = new DivWidget("Stops:");
    panel.add(label);

    DivPanel p2 = new DivPanel();
    panel.add(p2);

    _stopList = new ListBox();
    p2.add(_stopList);

    DivPanel p3 = new DivPanel();
    panel.add(p3);

    Anchor anchor = new Anchor("Find a new stop...");
    anchor.addClickListener(new ClickListener() {
      public void onClick(Widget arg0) {
        showStopFinderDialog();
      }
    });
    p3.add(anchor);

  }

  private void showStopFinderDialog() {
    PopupPanel dialog = new PopupPanel(true, true);
    dialog.addStyleName("StopFinderDialog");

    final StopFinderImpl stopFinder = new StopFinderImpl(dialog);
    _contextManager.addContextListener(stopFinder);
    dialog.setWidget(stopFinder.getWidget());

    dialog.center();
    dialog.addPopupListener(new PopupListener() {
      public void onPopupClosed(PopupPanel arg0, boolean arg1) {
        _contextManager.removeContextListener(stopFinder);
      }
    });

  }

  private class StopFinderImpl extends StopFinderPresenter {

    private PopupPanel _dialog;

    public StopFinderImpl(PopupPanel dialog) {
      super(_contextManager);
      _dialog = dialog;
    }

    @Override
    public void queryStop(String stopId) {
      _stopList.addItem(stopId);
      _dialog.hide();
    }

    @Override
    protected void handleLinksForStopInfoWindow(final StopBean bean, FlowPanel panel) {

      Anchor anchor = new Anchor("Use this stop");
      anchor.addClickListener(new ClickListener() {
        public void onClick(Widget arg0) {
          queryStop(bean.getId());
        }
      });
      panel.add(anchor);
    }
  }
}
