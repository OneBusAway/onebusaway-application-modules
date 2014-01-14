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
package org.onebusaway.webapp.gwt.where_library.view.constraints;

import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.webapp.gwt.where_library.UserContext;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.maps.client.geocode.Geocoder;
import com.google.gwt.maps.client.geocode.LocationCallback;
import com.google.gwt.maps.client.geocode.Placemark;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SubmitButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;

public class SetInitialSearchLocationPopupPanel {

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private Geocoder _geocoder = new Geocoder();

  @UiField
  PopupPanel _popupPanel;

  @UiField
  FormPanel _formPanel;

  @UiField
  TextBox _textBox;
  
  @UiField
  SubmitButton _submitButton;

  @UiField
  Button _cancelButton;

  @UiField
  FlowPanel _errorPanel;

  private UserBean _user;

  public SetInitialSearchLocationPopupPanel() {
    uiBinder.createAndBindUi(this);

    _errorPanel.setVisible(false);

    _formPanel.getElement().setId("setLocationForm");
    _formPanel.addSubmitHandler(new SubmitHandler() {

      @Override
      public void onSubmit(SubmitEvent event) {
        event.cancel();
        _errorPanel.setVisible(false);
        String value = _textBox.getText();
        value = value.trim();
        if (value.length() > 0) {
          queryLocation(value);
        }
      }
    });
    
    _submitButton.getElement().setId("setLocationButton");

    _cancelButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent arg0) {
        _popupPanel.hide();
      }
    });
  }

  public void addCloseHandler(CloseHandler<PopupPanel> closeHandler) {
    _popupPanel.addCloseHandler(closeHandler);
  }

  public void show() {
    _popupPanel.show();
    DeferredCommand.addCommand(new Command() {
      @Override
      public void execute() {
        _popupPanel.center();
      }
    });
  }

  public UserBean getUser() {
    return _user;
  }

  private void queryLocation(String value) {
    _geocoder.getLocations(value, new LocationHandler());
  }

  private void setError(String error) {
    _errorPanel.clear();
    _errorPanel.setVisible(true);
    _errorPanel.add(new Label(error));
  }

  private class LocationHandler implements LocationCallback {
    @Override
    public void onSuccess(JsArray<Placemark> locations) {

      if (locations.length() > 0) {

        Placemark placemark = locations.get(0);
        LatLng p = placemark.getPoint();

        String name = placemark.getAddress();
        double lat = p.getLatitude();
        double lon = p.getLongitude();

        UserContext context = UserContext.getContext();
        context.setDefaultSearchLocationForCurrentUser(name, lat, lon, false,
            new UserHandler());
      } else {
        setError("We could not find that location");
      }
    }

    @Override
    public void onFailure(int statusCode) {
      setError("We could not find that location");
    }
  }

  private class UserHandler implements AsyncCallback<UserBean> {

    @Override
    public void onSuccess(UserBean user) {
      System.out.println("updated user");
      _user = user;
      _popupPanel.hide();
    }

    @Override
    public void onFailure(Throwable arg0) {
      _popupPanel.hide();
    }
  }

  interface MyUiBinder extends
      UiBinder<PopupPanel, SetInitialSearchLocationPopupPanel> {
  }

}
