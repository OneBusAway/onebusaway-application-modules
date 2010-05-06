/*
 * Copyright 2008 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
/**
 * 
 */
package org.onebusaway.where.web.common.client.view.constraints;

import org.onebusaway.common.web.common.client.context.Context;
import org.onebusaway.common.web.common.client.resources.CommonResources;
import org.onebusaway.common.web.common.client.widgets.DivWidget;
import org.onebusaway.common.web.common.client.widgets.SpanWidget;
import org.onebusaway.where.web.common.client.rpc.WhereServiceAsync;
import org.onebusaway.where.web.common.client.view.EWhereStopFinderSearchType;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.maps.client.InfoWindow;
import com.google.gwt.maps.client.InfoWindowContent;
import com.google.gwt.maps.client.event.MarkerClickHandler;
import com.google.gwt.maps.client.geocode.Geocoder;
import com.google.gwt.maps.client.geocode.LocationCallback;
import com.google.gwt.maps.client.geocode.Placemark;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.List;

public class AddressQueryConstraint extends AbstractConstraint {

  private Geocoder _geocoder = new Geocoder();

  private String _query;

  private LatLngBounds _view;

  public AddressQueryConstraint(String query) {
    _query = query;

    _view = LatLngBounds.newInstance();
    _view.extend(LatLng.newInstance(47.97430795395781, -121.79454591726969));
    _view.extend(LatLng.newInstance(47.152554314370924, -122.50104172828858));

    _geocoder.setViewport(_view);
  }

  public void update(Context context) {
    _stopFinder.setSearchText(EWhereStopFinderSearchType.ADDRESS, _query);
    _geocoder.getLocations(_query, new GeocoderResultHandler());
  }

  @Override
  public boolean equals(Object obj) {

    if (obj == null || !(obj instanceof AddressQueryConstraint))
      return false;

    AddressQueryConstraint aqc = (AddressQueryConstraint) obj;
    return _query.equals(aqc._query);
  }

  @Override
  public int hashCode() {
    return _query.hashCode();
  }

  private void handlePlacemarks(List<Placemark> placemarks, final PlacemarkEvents events) {

    FlowPanel addressPanel = new FlowPanel();
    addressPanel.addStyleName("StopFinder-AddressPanel");
    _resultsPanel.add(addressPanel);

    events.addPreAddressList(addressPanel);

    FlowPanel addressListWidget = new FlowPanel();
    addressPanel.add(addressListWidget);

    for (int i = 0; i < placemarks.size(); i++) {

      final Placemark mark = placemarks.get(i);
      events.handlePlacemarkPre(mark);

      String addressHtml = getMarkAsHtml(mark);

      final Marker marker = new Marker(mark.getPoint());
      _map.addOverlay(marker);

      // Construct the info window
      final FlowPanel w = new FlowPanel();
      w.addStyleName("StopFinder-AddressInfoWindow");
      w.add(new DivWidget(addressHtml));
      final LatLng p = mark.getPoint();
      events.handlePlacemarkInfoWindowPost(mark, w, marker);

      ClickListener handler = new ClickListener() {
        public void onClick(Widget sender) {
          events.handePlacemarkClickHandlerPre(mark, marker);
          InfoWindow window = _map.getInfoWindow();
          window.open(p, new InfoWindowContent(w));
        }
      };

      Image img = new Image(CommonResources.INSTANCE.getImageMarker().getUrl());
      img.addStyleName("StopFinder-AddressListEntryIcon");

      HorizontalPanel hp = new HorizontalPanel();
      hp.add(img);
      DivWidget div2 = new DivWidget(addressHtml);
      hp.add(div2);
      hp.addStyleName("StopFinder-AddressListEntry");

      img.addClickListener(handler);
      div2.addClickListener(handler);

      addressListWidget.add(hp);

      marker.addMarkerClickHandler(new MarkerClickHandler() {
        public void onClick(MarkerClickEvent event) {
          InfoWindow window = _map.getInfoWindow();
          window.open(p, new InfoWindowContent(w));
        }
      });
    }

    events.handlePost();

  }

  private String getMarkAsHtml(final Placemark mark) {
    return "<div>" + mark.getStreet() + "</div><div>" + mark.getCity() + ", " + mark.getState() + " "
        + mark.getPostalCode() + "</div>";
  }

  private class GeocoderResultHandler implements LocationCallback {

    public void onSuccess(JsArray<Placemark> placemarksArray) {

      List<Placemark> placemarks = new ArrayList<Placemark>();

      for (int i = 0; i < placemarksArray.length(); i++) {
        Placemark placemark = placemarksArray.get(i);
        if (_view.containsLatLng(placemark.getPoint()))
          placemarks.add(placemark);
      }

      if (placemarks.size() == 1) {

        handlePlacemarks(placemarks, new SinglePlacemarkEvents());

        // Search for the actual stops
        Placemark mark = placemarks.get(0);
        LatLng p = mark.getPoint();
        double lat = p.getLatitude();
        double lon = p.getLongitude();
        int accuracy = mark.getAccuracy();
        WhereServiceAsync service = WhereServiceAsync.SERVICE;
        service.getStopsByLocationAndAccuracy(lat, lon, accuracy, _stopsHandler);

      } else if (placemarks.size() > 1) {
        handlePlacemarks(placemarks, new MultiPlacemarkEvents());
      } else {
        System.err.println("  no results");
      }
    }

    public void onFailure(int statusCode) {
      System.err.println("yeah...");
    }

  }

  private class PlacemarkEvents {

    public void addPreAddressList(FlowPanel addressPanel) {

    }

    public void handePlacemarkClickHandlerPre(Placemark mark, Marker marker) {

    }

    public void handlePlacemarkPre(Placemark mark) {

    }

    public void handlePlacemarkInfoWindowPost(Placemark mark, FlowPanel w, Marker marker) {

    }

    public void handlePost() {

    }

  }

  private class SinglePlacemarkEvents extends PlacemarkEvents {

    private boolean _isActive = true;

    private SpanWidget _span;

    public void addPreAddressList(FlowPanel addressPanel) {
      addressPanel.add(new DivWidget(_msgs.standardIndexPageAddressFound()));
    }

    @Override
    public void handlePlacemarkInfoWindowPost(Placemark mark, FlowPanel w, final Marker marker) {
      _span = new SpanWidget(_msgs.standardIndexPageHideThisMarker());
      _span.addStyleName("StopFinder-HideThisMarker");
      _span.addClickListener(new ClickListener() {
        public void onClick(Widget arg0) {
          InfoWindow window = _map.getInfoWindow();
          window.close();
          _map.removeOverlay(marker);
          _isActive = false;
          _span.setVisible(false);
        }
      });
      w.add(_span);
    }

    public void handePlacemarkClickHandlerPre(Placemark mark, Marker marker) {
      if (!_isActive) {
        _map.addOverlay(marker);
        _isActive = true;
      }
      _span.setVisible(true);
    }
  }

  private class MultiPlacemarkEvents extends PlacemarkEvents {

    private LatLngBounds _bounds = LatLngBounds.newInstance();

    public void addPreAddressList(FlowPanel addressPanel) {
      addressPanel.add(new DivWidget(_msgs.standardIndexPageDidYouMean()));
    }

    @Override
    public void handlePlacemarkPre(Placemark mark) {
      _bounds.extend(mark.getPoint());
    }

    @Override
    public void handlePlacemarkInfoWindowPost(final Placemark mark, FlowPanel w, Marker marker) {
      Anchor anchor = new Anchor(_msgs.standardIndexPageFindNearbyStops());
      anchor.addClickListener(new ClickListener() {
        public void onClick(Widget arg0) {
          _stopFinder.queryLocation(mark.getPoint(), mark.getAccuracy());
        }
      });
      w.add(anchor);
    }

    @Override
    public void handlePost() {

      // Adjust the map to fit the contents
      int zoom = _map.getBoundsZoomLevel(_bounds);
      _map.setCenter(_bounds.getCenter(), zoom);
    }

  }

}