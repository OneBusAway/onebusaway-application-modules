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
package org.onebusaway.webapp.gwt.where_standard;

import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.webapp.gwt.common.context.Context;
import org.onebusaway.webapp.gwt.common.context.ContextManager;
import org.onebusaway.webapp.gwt.common.context.DirectContextManager;
import org.onebusaway.webapp.gwt.common.widgets.DivPanel;
import org.onebusaway.webapp.gwt.common.widgets.DivWidget;
import org.onebusaway.webapp.gwt.where_library.view.StopFinderPresenter;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;

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
    anchor.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent arg0) {
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
    Context context = _contextManager.getContext();
    dialog.setWidget(stopFinder.initialize(context));

    dialog.center();
    dialog.addCloseHandler(new CloseHandler<PopupPanel>() {

      public void onClose(CloseEvent<PopupPanel> arg0) {
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
    protected void handleLinksForStopInfoWindow(final StopBean bean,
        FlowPanel panel) {

      Anchor anchor = new Anchor("Use this stop");
      anchor.addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent arg0) {
          queryStop(bean.getId());
        }
      });
      panel.add(anchor);
    }
  }
}
