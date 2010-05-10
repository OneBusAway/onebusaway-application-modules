package org.onebusaway.webapp.gwt.login_widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;

public class OpenIdWidget extends Composite implements
    HasCloseHandlers<OpenIdWidget> {

  private static MyUiBinder _uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  FormPanel _formPanel;

  @UiField
  TextBox _textBox;

  @UiField
  Button _okButton;

  @UiField
  Button _cancelButton;

  private String _baseUrl;

  private String _target;

  public OpenIdWidget(String baseUrl, String target) {
    initWidget(_uiBinder.createAndBindUi(this));
    _baseUrl = baseUrl;
    _target = target;
  }

  @Override
  public HandlerRegistration addCloseHandler(CloseHandler<OpenIdWidget> handler) {
    return addHandler(handler, CloseEvent.getType());
  }

  @UiHandler("_formPanel")
  public void onFormSubmit(SubmitEvent event) {
    event.cancel();

    String openIdUrl = _textBox.getText().trim();
    if (openIdUrl.length() == 0)
      return;
    String url = _baseUrl + "/openid?url=" + openIdUrl + "&target=" + _target;
    Location.assign(url);
  }

  @UiHandler("_cancelButton")
  public void onCancelButtonClicked(ClickEvent event) {
    CloseEvent.fire(this, this);
  }

  interface MyUiBinder extends UiBinder<Widget, OpenIdWidget> {
  }

}
