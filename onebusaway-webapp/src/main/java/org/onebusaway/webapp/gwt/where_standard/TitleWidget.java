package org.onebusaway.webapp.gwt.where_standard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;

public class TitleWidget extends Widget {

  interface MyUiBinder extends UiBinder<DivElement, TitleWidget> {}
  
  private static MyUiBinder _uiBinder = GWT.create(MyUiBinder.class);

  public TitleWidget() {
    setElement(_uiBinder.createAndBindUi(this));
  }
}