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
/**
 * 
 */
package org.onebusaway.webapp.gwt.where_library.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.onebusaway.transit_data.model.RoutesAndStopsBean;
import org.onebusaway.webapp.gwt.common.control.Place;
import org.onebusaway.webapp.gwt.common.control.PlaceImpl;
import org.onebusaway.webapp.gwt.common.control.PlaceSearch;
import org.onebusaway.webapp.gwt.where_library.services.CombinedSearchResult;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.maps.client.geocode.LocationCallback;
import com.google.gwt.maps.client.geocode.Placemark;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.search.client.SearchCompleteHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

class CombinedSearchHandlerImpl extends Timer implements
    AsyncCallback<RoutesAndStopsBean>, SearchCompleteHandler, LocationCallback {

  private static final double PLACE_MERGE_DISTANCE = 25.0;

  private LatLngBounds _bounds;

  private AsyncCallback<CombinedSearchResult> _callback;

  private boolean _resultsSent = false;

  private boolean _routeAndStopResultReceived = false;

  private boolean _addressResultsReceived = false;

  private boolean _localResultsReceived = false;

  private boolean _placeSearchTimeout = false;

  private RoutesAndStopsBean _routeAndStopResult;

  private List<Place> _addresses = new ArrayList<Place>();

  private List<Place> _places = new ArrayList<Place>();

  public CombinedSearchHandlerImpl(LatLngBounds bounds, int placeTimeoutMillis,
      AsyncCallback<CombinedSearchResult> callback) {
    _bounds = bounds;
    _callback = callback;
    schedule(placeTimeoutMillis);
  }

  /****
   * {@link Timer} Interface
   ****/

  /**
   * This is fired when the timer has expired
   */
  @Override
  public void run() {
    _placeSearchTimeout = true;
    checkAndSendResults();
  }

  /****
   * {@link AsyncCallback} Interface
   ****/

  @Override
  public void onSuccess(RoutesAndStopsBean routesAndStopsResult) {
    _routeAndStopResultReceived = true;
    _routeAndStopResult = routesAndStopsResult;
    checkAndSendResults();
  }

  @Override
  public void onFailure(Throwable ex) {
    _callback.onFailure(ex);
  }

  /****
   * {@link LocationCallback} Interface
   ****/

  @Override
  public void onSuccess(JsArray<Placemark> placemarks) {

    for (int i = 0; i < placemarks.length(); i++) {
      Placemark mark = placemarks.get(i);
      Place place = getPlacemarkAsPlace(mark);
      _addresses.add(place);
      System.out.println("address=" + place);
    }

    _addressResultsReceived = true;
    checkAndSendResults();
  }

  @Override
  public void onFailure(int statusCode) {
    System.err.println("address search failure: code=" + statusCode);
  }

  /****
   * {@link SearchCompleteHandler} Interface
   ****/

  @Override
  public void onSearchComplete(SearchCompleteEvent event) {

    System.out.println("search event=" + event);

    PlaceSearch.getSeachCompleteEventAsPlaces(event, _places);

    _localResultsReceived = true;
    checkAndSendResults();
  }

  /****
   * Private Methods
   ****/

  private void checkAndSendResults() {

    boolean placeResults = (_addressResultsReceived && _localResultsReceived)
        || _placeSearchTimeout;

    if (_routeAndStopResultReceived && placeResults)
      sendResults();
  }

  private void sendResults() {

    if (_resultsSent)
      return;

    _resultsSent = true;

    _addresses = consolidatePlaces(_addresses);
    _places = consolidatePlaces(_places);
    consolidatePlacesNearAddresses();

    CombinedSearchResult results = new CombinedSearchResult();
    if (_routeAndStopResult != null) {
      results.setRoutes(_routeAndStopResult.getRoutes().getRoutes());
      results.setStops(_routeAndStopResult.getStops().getStops());
    }

    results.setAddresses(_addresses);
    results.setPlaces(_places);

    _callback.onSuccess(results);
  }

  private Place getPlacemarkAsPlace(Placemark mark) {

    String name = null;
    List<String> description = new ArrayList<String>();

    if (mark.getStreet() != null) {
      name = mark.getStreet();
      if (mark.getCity() != null)
        description.add(mark.getCity());
      if (mark.getState() != null)
        description.add(mark.getState());
      if (mark.getPostalCode() != null)
        description.add(mark.getPostalCode());
    } else if (mark.getCity() != null) {
      name = mark.getCity();
      if (mark.getState() != null)
        name += ", " + mark.getState();
    } else if (mark.getState() != null) {
      name = mark.getState();
      if (mark.getCounty() != null)
        description.add(mark.getCountry());
    }

    return new PlaceImpl(name, description, mark.getPoint(), mark.getAccuracy());
  }

  private List<Place> consolidatePlaces(List<Place> places) {

    for (Iterator<Place> it = places.iterator(); it.hasNext();) {
      Place place = it.next();
      if (place.getName() == null)
        it.remove();
    }

    // Merge nearby place results that are very similar
    places = PlaceSearch.mergeNearbyEntries(places, PLACE_MERGE_DISTANCE);

    List<Place> placesInRange = new ArrayList<Place>();
    if (_bounds != null) {
      for (Place place : places) {
        if (_bounds.containsLatLng(place.getLocation()))
          placesInRange.add(place);
      }
    }

    if (!placesInRange.isEmpty())
      places = placesInRange;

    return places;
  }

  private void consolidatePlacesNearAddresses() {
    for (Iterator<Place> it = _places.iterator(); it.hasNext();) {
      Place place = it.next();
      for (Place address : _addresses) {
        double distance = place.getLocation().distanceFrom(
            address.getLocation());
        if (distance < PLACE_MERGE_DISTANCE) {
          it.remove();
          continue;
        }
      }
    }
  }
}