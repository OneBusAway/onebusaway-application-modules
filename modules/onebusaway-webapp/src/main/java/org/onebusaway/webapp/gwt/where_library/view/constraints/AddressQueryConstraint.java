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
package org.onebusaway.webapp.gwt.where_library.view.constraints;

import org.onebusaway.webapp.gwt.common.context.Context;
import org.onebusaway.webapp.gwt.common.control.Place;
import org.onebusaway.webapp.gwt.common.control.PlaceSearch;
import org.onebusaway.webapp.gwt.common.control.PlaceSearchListener;
import org.onebusaway.webapp.gwt.common.resources.map.MapResources;
import org.onebusaway.webapp.gwt.common.widgets.DivPanel;
import org.onebusaway.webapp.gwt.common.widgets.DivWidget;
import org.onebusaway.webapp.gwt.common.widgets.PlacePresenter;
import org.onebusaway.webapp.gwt.where_library.resources.StopFinderCssResource;
import org.onebusaway.webapp.gwt.where_library.resources.StopFinderResources;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.maps.client.InfoWindow;
import com.google.gwt.maps.client.InfoWindowContent;
import com.google.gwt.maps.client.event.MarkerClickHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.List;

public class AddressQueryConstraint extends AbstractConstraint {

  private static PlaceSearch _placeSearch = new PlaceSearch();

  private static PlacePresenter _placePresenter = new PlacePresenter();

  private static StopFinderCssResource _css = StopFinderResources.INSTANCE.getCSS();

  private String _query;

  private LatLngBounds _view;

  public AddressQueryConstraint(String query) {
    _query = query;

    _view = LatLngBounds.newInstance();
    _view.extend(LatLng.newInstance(47.97430795395781, -121.79454591726969));
    _view.extend(LatLng.newInstance(47.152554314370924, -122.50104172828858));
  }

  public void update(Context context) {
    _stopFinder.setSearchText(_query);
    _placeSearch.query(_query, new PlaceSearchHandler(), _view);
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

  private void handlePlacemarks(List<Place> placemarks, final PlaceEvents events) {

    FlowPanel addressPanel = new FlowPanel();
    _resultsPanel.add(addressPanel);

    events.addPreAddressList(addressPanel);

    FlowPanel addressListWidget = new FlowPanel();
    addressPanel.add(addressListWidget);

    for (int i = 0; i < placemarks.size(); i++) {

      final Place place = placemarks.get(i);
      events.handlePlacemarkPre(place);

      final LatLng p = place.getLocation();
      final Marker marker = new Marker(p);
      _map.addOverlay(marker);

      // Construct the info window
      final FlowPanel w = new FlowPanel();
      w.addStyleName(_css.stopFinderAddressInfoWindow());
      w.add(events.constructPlacemarkInfoWindowWidget(place, marker));

      marker.addMarkerClickHandler(new MarkerClickHandler() {
        public void onClick(MarkerClickEvent event) {
          events.handlePlacemarkClickHandlerPre(place, marker);
          InfoWindow window = _map.getInfoWindow();
          window.open(p, new InfoWindowContent(w));
        }
      });

      // Construct the address list entry
      DivPanel addressListEntryPanel = new DivPanel();
      addressListEntryPanel.addStyleName(_css.stopFinderAddressListEntry());

      ClickHandler handler = new ClickHandler() {
        public void onClick(ClickEvent sender) {
          events.handlePlacemarkClickHandlerPre(place, marker);
          InfoWindow window = _map.getInfoWindow();
          window.open(p, new InfoWindowContent(w));
        }
      };

      Image img = new Image(MapResources.INSTANCE.getImageMarker().getUrl());
      img.addStyleName(_css.stopFinderAddressListEntryIcon());
      img.addClickHandler(handler);
      addressListEntryPanel.add(img);

      DivPanel placePanel = _placePresenter.getPlaceAsPanel(place, handler);
      placePanel.addStyleName(_css.stopFinderAddressListEntryPlace());
      addressListEntryPanel.add(placePanel);

      addressListWidget.add(addressListEntryPanel);

    }

    events.handlePost();

  }

  private class PlaceSearchHandler implements PlaceSearchListener {

    public void onFailure(int statusCode) {
      System.err.println("yeah...");
    }

    public void handleError() {
      System.err.println("yeah...");
    }

    public void handleMultipleResults(List<Place> places) {
      handlePlacemarks(places, new MultiPlacemarkEvents());
    }

    public void handleNoResult() {
      System.err.println("  no results");
    }

    public void handleSingleResult(Place place) {

      List<Place> places = new ArrayList<Place>(1);
      places.add(place);
      handlePlacemarks(places, new SinglePlacemarkEvents());

      // Search for the actual stops
      LatLng p = place.getLocation();
      int accuracy = place.getAccuracy();
      _stopFinder.setCenter(p, accuracy);
    }
  }

  private class PlaceEvents {

    public void addPreAddressList(FlowPanel addressPanel) {

    }

    public void handlePlacemarkClickHandlerPre(Place mark, Marker marker) {

    }

    public void handlePlacemarkPre(Place mark) {

    }

    public Widget constructPlacemarkInfoWindowWidget(Place place, Marker marker) {
      throw new UnsupportedOperationException();
    }

    public void handlePost() {

    }

  }

  private class SinglePlacemarkEvents extends PlaceEvents {

    private boolean _isActive = true;

    private DivPanel _hideTheMarkerAnchor;

    @Override
    public void addPreAddressList(FlowPanel addressPanel) {
      addressPanel.add(new DivWidget(_msgs.standardIndexPageAddressFound()));
    }

    @Override
    public Widget constructPlacemarkInfoWindowWidget(Place place,
        final Marker marker) {

      DivPanel panel = _placePresenter.getPlaceAsPanel(place);

      _hideTheMarkerAnchor = new DivPanel();
      _hideTheMarkerAnchor.addStyleName(_css.stopFinderHideThisMarker());
      panel.add(_hideTheMarkerAnchor);

      Anchor anchor = new Anchor(_msgs.standardIndexPageHideThisMarker());
      _hideTheMarkerAnchor.add(anchor);
      anchor.addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent arg0) {
          InfoWindow window = _map.getInfoWindow();
          window.close();
          marker.setVisible(false);
          _isActive = false;
          _hideTheMarkerAnchor.setVisible(false);
        }
      });

      return panel;
    }

    @Override
    public void handlePlacemarkClickHandlerPre(Place mark, Marker marker) {
      if (!_isActive) {
        marker.setVisible(true);
        _isActive = true;
        _hideTheMarkerAnchor.setVisible(true);
      }
    }
  }

  private class MultiPlacemarkEvents extends PlaceEvents {

    private LatLngBounds _bounds = LatLngBounds.newInstance();

    public void addPreAddressList(FlowPanel addressPanel) {
      addressPanel.add(new DivWidget(_msgs.standardIndexPageDidYouMean()));
    }

    @Override
    public void handlePlacemarkPre(Place mark) {
      _bounds.extend(mark.getLocation());
    }

    @Override
    public Widget constructPlacemarkInfoWindowWidget(final Place place,
        Marker marker) {

      ClickHandler handler = new ClickHandlerImpl(place);
      DivPanel panel = _placePresenter.getPlaceAsPanel(place, handler);

      DivPanel nearby = new DivPanel();
      nearby.addStyleName(_css.stopFinderFindNearbyStops());
      Anchor anchor = new Anchor(_msgs.standardIndexPageFindNearbyStops());
      anchor.addClickHandler(handler);
      nearby.add(anchor);

      panel.add(nearby);

      return panel;
    }

    @Override
    public void handlePost() {

      // Adjust the map to fit the contents
      if (!_bounds.isEmpty()) {
        int zoom = _map.getBoundsZoomLevel(_bounds);
        _map.setCenter(_bounds.getCenter(), zoom);
      }
    }

    private final class ClickHandlerImpl implements ClickHandler {

      private final Place _mark;

      private ClickHandlerImpl(Place mark) {
        _mark = mark;
      }

      public void onClick(ClickEvent arg0) {
        _stopFinder.queryLocation(_mark.getLocation(), _mark.getAccuracy());
      }
    }
  }

}