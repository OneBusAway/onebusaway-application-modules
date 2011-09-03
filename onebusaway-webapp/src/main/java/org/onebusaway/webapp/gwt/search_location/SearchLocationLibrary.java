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
package org.onebusaway.webapp.gwt.search_location;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.maps.client.geocode.Geocoder;
import com.google.gwt.maps.client.geocode.LocationCallback;
import com.google.gwt.maps.client.geocode.Placemark;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SubmitButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;

public class SearchLocationLibrary implements EntryPoint {

  @Override
  public void onModuleLoad() {
    exportStaticMethod();
    addSearchLocationHandler("changeSearchLocation");
  }

  public static native void exportStaticMethod()
  /*-{
    $wnd.oba_addSearchLocationHandler = $entry(@org.onebusaway.webapp.gwt.search_location.SearchLocationLibrary::addSearchLocationHandler(Ljava/lang/String;));
  }-*/;

  public static void addSearchLocationHandler(String containerElementId) {
    System.out.println("container=" + containerElementId);
    RootPanel panel = RootPanel.get(containerElementId);
    if (panel != null)
      SearchLocationHandler.wire(panel);
  }

  private static class SearchLocationHandler implements LocationCallback {

    private RootPanel _panel;

    private Geocoder _geocoder = new Geocoder();

    private String _query;

    public static void wire(RootPanel panel) {
      SearchLocationHandler handler = new SearchLocationHandler(panel);
      handler.setChangeDefaultSearchLocationMode();
    }

    private SearchLocationHandler(RootPanel panel) {
      _panel = panel;
    }

    private void setChangeDefaultSearchLocationMode() {

      Anchor anchor = new Anchor("Change your default search location");

      _panel.clear();
      _panel.add(anchor);

      anchor.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent arg0) {
          setQuerySearchLocationMode();
        }
      });
    }

    private void setQuerySearchLocationMode() {

      _panel.clear();
      _query = null;

      FormPanel form = new FormPanel();
      _panel.add(form);

      FlowPanel row = new FlowPanel();
      form.add(row);

      final TextBox textBox = new TextBox();
      row.add(textBox);

      SubmitButton submitButton = new SubmitButton("Set location");
      row.add(submitButton);

      Button cancelButton = new Button("Cancel");
      row.add(cancelButton);

      System.out.println("here: 7");

      form.addSubmitHandler(new SubmitHandler() {

        @Override
        public void onSubmit(SubmitEvent event) {
          event.cancel();
          String value = textBox.getText();
          value = value.trim();
          if (value.length() > 0) {
            queryLocation(value);
          }
        }
      });

      cancelButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent arg0) {
          setChangeDefaultSearchLocationMode();
        }
      });
    }

    private void queryLocation(String value) {
      _query = value;
      _geocoder.getLocations(value, this);
    }

    @Override
    public void onSuccess(JsArray<Placemark> locations) {
      if (locations.length() > 0) {
        Placemark placemark = locations.get(0);
        LatLng p = placemark.getPoint();
        String name = URL.encode(_query);
        String lat = URL.encode(Double.toString(p.getLatitude()));
        String lon = URL.encode(Double.toString(p.getLongitude()));
        Window.Location.replace("set-default-location.action?name=" + name
            + "&lat=" + lat + "&lon=" + lon);
      } else {
        setChangeDefaultSearchLocationMode();
      }
    }

    @Override
    public void onFailure(int statusCode) {
      setChangeDefaultSearchLocationMode();
    }
  }
}
