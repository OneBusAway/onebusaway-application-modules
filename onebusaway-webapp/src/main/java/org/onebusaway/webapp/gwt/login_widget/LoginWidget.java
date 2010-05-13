package org.onebusaway.webapp.gwt.login_widget;

import org.onebusaway.webapp.gwt.common.widgets.AnchorPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

public class LoginWidget extends Composite {

  private static MyUiBinder _uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  FlowPanel _parentPanel;

  @UiField
  HTMLPanel _mainPanel;

  @UiField
  AnchorElement _googleLink;
  
  @UiField
  AnchorElement _facebookLink;

  @UiField
  AnchorElement _twitterLink;

  @UiField
  AnchorElement _yahooLink;

  @UiField
  AnchorPanel _aolLink;

  @UiField
  AnchorPanel _openIdLink;

  private String _baseUrl;

  private String _target;

  public LoginWidget(String baseUrl, String target) {
    initWidget(_uiBinder.createAndBindUi(this));

    _baseUrl = baseUrl;
    _target = target;

    setOpenIdHref(_googleLink, "https://www.google.com/accounts/o8/id");
    setOpenIdHref(_yahooLink, "http://yahoo.com/");
    _facebookLink.setHref(_baseUrl + "/facebook?target=" + _target);
    _twitterLink.setHref(_baseUrl + "/twitter?target=" + _target);
    
  }

  @UiHandler("_aolLink")
  public void onAolClick(ClickEvent event) {
    AolWidget widget = new AolWidget(_baseUrl, _target);
    _mainPanel.setVisible(false);
    _parentPanel.add(widget);

    widget.addCloseHandler(new CloseHandler<AolWidget>() {
      @Override
      public void onClose(CloseEvent<AolWidget> event) {
        _parentPanel.remove(event.getTarget());
        _mainPanel.setVisible(true);
      }
    });
  }

  @UiHandler("_openIdLink")
  public void onOpenIdClick(ClickEvent event) {
    OpenIdWidget widget = new OpenIdWidget(_baseUrl, _target);
    _mainPanel.setVisible(false);
    _parentPanel.add(widget);

    widget.addCloseHandler(new CloseHandler<OpenIdWidget>() {
      @Override
      public void onClose(CloseEvent<OpenIdWidget> event) {
        _parentPanel.remove(event.getTarget());
        _mainPanel.setVisible(true);
      }
    });
  }

  private void setOpenIdHref(AnchorElement anchor, String openIdUrl) {
    String url = _baseUrl + "/openid?url=" + openIdUrl + "&target=" + _target;
    anchor.setHref(url);
  }

  interface MyUiBinder extends UiBinder<Widget, LoginWidget> {
  }

}
