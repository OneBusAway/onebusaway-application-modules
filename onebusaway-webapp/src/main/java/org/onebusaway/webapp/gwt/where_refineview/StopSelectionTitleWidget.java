package org.onebusaway.webapp.gwt.where_refineview;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ModalLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class StopSelectionTitleWidget extends Composite {

  interface MyUiBinder extends UiBinder<Widget, StopSelectionTitleWidget> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  Anchor cancelAnchor;

  private ModalLayoutPanel _dialog;

  public StopSelectionTitleWidget(ModalLayoutPanel dialog) {
    initWidget(uiBinder.createAndBindUi(this));
    cancelAnchor.setHref("#goBack");
    _dialog = dialog;
  }

  @UiHandler("cancelAnchor")
  void handleClick(ClickEvent e) {
    e.preventDefault();
    _dialog.hide();
  }
}
