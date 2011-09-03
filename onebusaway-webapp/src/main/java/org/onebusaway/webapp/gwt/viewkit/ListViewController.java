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

import org.onebusaway.webapp.gwt.common.widgets.SpanWidget;
import org.onebusaway.webapp.gwt.viewkit.resources.ViewKitCssResource;
import org.onebusaway.webapp.gwt.viewkit.resources.ViewKitResources;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class ListViewController extends ViewController {

  private static final ViewKitCssResource _css = ViewKitResources.INSTANCE.getCSS();

  private FlowPanel _panel = new FlowPanel();

  private ListViewModel _model = new ListViewModel();

  private IndexPath _selectionIndex = null;

  public void setModel(ListViewModel model) {
    _model = model;
  }

  public IndexPath getSelectionIndex() {
    return _selectionIndex;
  }

  public void refreshModel() {

    _model.setListViewController(this);
    _model.willReload();

    _panel.clear();

    int sectionCount = _model.getNumberOfSections();

    for (int sectionIndex = 0; sectionIndex < sectionCount; sectionIndex++) {

      int rowCount = _model.getNumberOfRowsInSection(sectionIndex);

      if (rowCount <= 0)
        continue;

      FlowPanel sectionPanel = new FlowPanel();
      sectionPanel.addStyleName(_css.ListViewSection());
      _panel.add(sectionPanel);

      for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {

        ClickableFlowPanel rowPanel = new ClickableFlowPanel();
        rowPanel.addStyleName(_css.ListViewRow());
        if (rowIndex == 0)
          rowPanel.addStyleName(_css.ListViewRowFirst());
        if (rowIndex == rowCount - 1)
          rowPanel.addStyleName(_css.ListViewRowLast());

        sectionPanel.add(rowPanel);

        ListViewRow row = _model.getListViewRowForSectionAndRow(sectionIndex,
            rowIndex);

        addRowContentToRowPanel(sectionIndex, rowIndex, row, rowPanel);
      }
    }

    _model.didReload();
  }

  /****
   * {@link ViewController} Interface
   ****/

  @Override
  public void viewWillAppear() {
    super.viewWillAppear();
    _selectionIndex = null;
  }

  @Override
  protected void loadView() {
    super.loadView();
    _panel.addStyleName(_css.ListViewController());
    _view = _panel;
  }

  @Override
  public void handleContext(List<String> path, Map<String, String> context) {
    _model.handleContext(path, context);
  }

  @Override
  public void retrieveContext(List<String> path, Map<String, String> context) {
    _model.retrieveContext(path, context);
  }

  /****
   * Private Methods
   ****/

  private void addRowContentToRowPanel(final int sectionIndex,
      final int rowIndex, ListViewRow row, ClickableFlowPanel rowPanel) {

    Widget customView = row.getCustomView();

    if (customView != null) {
      rowPanel.add(customView);
    } else {

      switch (row.getStyle()) {
        case DEFAULT: {
          FlowPanel text = new FlowPanel();
          text.addStyleName(_css.ListViewRowText());
          text.add(new SpanWidget(row.getText()));
          rowPanel.add(text);
          break;
        }

        case DETAIL: {
          FlowPanel text = new FlowPanel();
          text.add(new SpanWidget(row.getText()));
          text.addStyleName(_css.ListViewRowText());
          rowPanel.add(text);

          FlowPanel detail = new FlowPanel();
          detail.add(new SpanWidget(row.getDetailText()));
          detail.addStyleName(_css.ListViewRowDetailText());
          rowPanel.add(detail);

          break;
        }
      }
    }

    if (_model.willRespondToRowClicks()) {
      rowPanel.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent arg0) {
          _selectionIndex = new IndexPath(sectionIndex, rowIndex);
          _model.onRowClick(ListViewController.this, sectionIndex, rowIndex);
        }
      });
    }
  }
}
