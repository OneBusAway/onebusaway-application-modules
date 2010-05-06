package org.onebusaway.webapp.gwt.where_library.view.constraints;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

public class PopupDialogTest extends Composite {

  private static MyUiBinder _uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  PopupPanel _panel;
  
  public PopupDialogTest() {
    initWidget(_uiBinder.createAndBindUi(this));
  }
  
  public void show() {
    _panel.show();
  }

  interface MyUiBinder extends UiBinder<Widget, PopupDialogTest> {
  }

}
