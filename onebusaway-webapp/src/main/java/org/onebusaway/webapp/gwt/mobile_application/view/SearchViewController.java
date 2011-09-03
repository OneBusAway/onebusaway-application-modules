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
package org.onebusaway.webapp.gwt.mobile_application.view;

import java.util.Map;

import org.onebusaway.webapp.gwt.common.widgets.DivWidget;
import org.onebusaway.webapp.gwt.mobile_application.control.Actions;
import org.onebusaway.webapp.gwt.mobile_application.resources.MobileApplicationCssResource;
import org.onebusaway.webapp.gwt.mobile_application.resources.MobileApplicationResources;
import org.onebusaway.webapp.gwt.viewkit.ViewController;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;

public class SearchViewController extends ViewController {

  private static final MobileApplicationCssResource _css = MobileApplicationResources.INSTANCE.getCSS();

  private enum ESearchType {
    ROUTE, ADDRESS, STOP
  }

  private Map<ESearchType, ToggleButton> _buttonsByType = new java.util.HashMap<ESearchType, ToggleButton>();

  private ESearchType _currentType = null;

  private TextBox _input;

  @Override
  protected void loadView() {
    super.loadView();

    getNavigationItem().setTitle("Search");

    FlowPanel panel = new FlowPanel();
    panel.add(new DivWidget("Search by:"));

    Grid buttonRow = new Grid(1, 3);
    buttonRow.addStyleName(_css.SearchTypeButtons());
    panel.add(buttonRow);

    buttonRow.setWidget(0, 0, createButton(ESearchType.ROUTE, "Route"));
    buttonRow.setWidget(0, 1, createButton(ESearchType.ADDRESS, "Address"));
    buttonRow.setWidget(0, 2, createButton(ESearchType.STOP, "Stop #"));

    setEnabledButton(ESearchType.ROUTE);


    FormPanel form = new FormPanel();
    panel.add(form);

    FlowPanel formRow = new FlowPanel();
    form.add(formRow);

    _input = new TextBox();
    _input.addStyleName(_css.SearchTextBox());
    formRow.add(_input);
    
    Button submitButton = new Button("Search");
    submitButton.addStyleName(_css.SearchSubmitButton());
    formRow.add(submitButton);

    SearchHandler handler = new SearchHandler();
    form.addSubmitHandler(handler);
    submitButton.addClickHandler(handler);
    
    _view = panel;
  }

  private ToggleButton createButton(final ESearchType type, String label) {
    ToggleButton button = new ToggleButton(label);
    button.addStyleName(_css.SearchTypeButton());
    button.setDown(false);
    button.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent arg0) {
        setEnabledButton(type);
      }
    });
    _buttonsByType.put(type, button);
    return button;
  }

  private void setEnabledButton(ESearchType type) {
    for (Map.Entry<ESearchType, ToggleButton> entry : _buttonsByType.entrySet()) {
      ESearchType searchType = entry.getKey();
      ToggleButton button = entry.getValue();
      button.setDown(searchType.equals(type));
    }
    _currentType = type;
  }
  
  private void search() {
    
    String text = _input.getText();
    if( text.length() == 0)
      return;
    
    System.out.println(_currentType + " " + text);
    
    switch(_currentType) {
      case ROUTE:
        Actions.searchForRoute(text);
        break;
      case ADDRESS:
        Actions.searchForAddress(text);
        break;
      case STOP:
        Actions.showArrivalsAndDeparturesForStop(getNavigationController(), "1_" + text);
    }
    
  }

  private class SearchHandler implements ClickHandler, SubmitHandler {

    @Override
    public void onClick(ClickEvent arg0) {
      search();
    }

    @Override
    public void onSubmit(SubmitEvent event) {
      event.cancel();
      search();
    }
  }
}
