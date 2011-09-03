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

import org.onebusaway.webapp.gwt.common.widgets.DivWidget;
import org.onebusaway.webapp.gwt.mobile_application.MobileApplicationContext;
import org.onebusaway.webapp.gwt.mobile_application.control.MobileApplicationDao;
import org.onebusaway.webapp.gwt.mobile_application.model.Bookmark;
import org.onebusaway.webapp.gwt.mobile_application.resources.MobileApplicationCssResource;
import org.onebusaway.webapp.gwt.mobile_application.resources.MobileApplicationResources;
import org.onebusaway.webapp.gwt.viewkit.BarButtonItem;
import org.onebusaway.webapp.gwt.viewkit.NavigationController;
import org.onebusaway.webapp.gwt.viewkit.ViewController;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;

public class BookmarkViewController extends ViewController {

  private static final MobileApplicationCssResource _css = MobileApplicationResources.INSTANCE.getCSS();

  private Bookmark _bookmark;

  private boolean _newBookmark;

  private TextBox _input;

  public BookmarkViewController(Bookmark bookmark, boolean newBookmark) {
    _bookmark = bookmark;
    _newBookmark = newBookmark;
    
  }

  @Override
  protected void loadView() {
    super.loadView();

    getNavigationItem().setTitle(
        _newBookmark ? "Add Bookmark" : "Edit Bookmark");
    getNavigationItem().setRightBarButtonItem(
        new BarButtonItem("Cancel", new CancelHandler()));

    FlowPanel panel = new FlowPanel();
    panel.add(new DivWidget("Name:"));

    FormPanel form = new FormPanel();
    panel.add(form);

    FlowPanel formRow = new FlowPanel();
    form.add(formRow);

    _input = new TextBox();
    _input.setText(_bookmark.getName());
    _input.addStyleName(_css.BookmarkTextBox());
    formRow.add(_input);

    Button submitButton = new Button("Save");
    submitButton.addStyleName(_css.BookmarkSaveButton());
    formRow.add(submitButton);

    SaveHandler handler = new SaveHandler();
    form.addSubmitHandler(handler);
    submitButton.addClickHandler(handler);

    _view = panel;
  }

  private void save() {

    String text = _input.getText();
    if (text.length() == 0)
      return;

    _bookmark.setName(text);
    
    if( _newBookmark ) {
      MobileApplicationDao dao = MobileApplicationContext.getDao();
      dao.addBookmark(_bookmark);
    }
      
    NavigationController nav = getNavigationController();
    nav.popViewController();
  }

  private class SaveHandler implements ClickHandler, SubmitHandler {

    @Override
    public void onClick(ClickEvent arg0) {
      save();
    }

    @Override
    public void onSubmit(SubmitEvent event) {
      event.cancel();
      save();
    }
  }
  
  private class CancelHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent arg0) {
      NavigationController nav = getNavigationController();
      nav.popViewController();
    }
  }
}
