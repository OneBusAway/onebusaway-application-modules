package org.onebusaway.webapp.gwt.where_library.view.constraints;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ParagraphElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

public class DefaultSearchLocationWidget extends Widget {

  private static MyUiBinder _uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  SpanElement _defaultSearchLocationName;

  public DefaultSearchLocationWidget(String name) {
    setElement(_uiBinder.createAndBindUi(this));
    _defaultSearchLocationName.setInnerText(name);
  }

  interface MyUiBinder extends
      UiBinder<ParagraphElement, DefaultSearchLocationWidget> {
  }

}
