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
package org.onebusaway.webapp.gwt.common.control;

import org.onebusaway.utility.collections.TreeUnionFind;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.maps.client.geocode.Geocoder;
import com.google.gwt.maps.client.geocode.LocationCallback;
import com.google.gwt.maps.client.geocode.Placemark;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.search.client.AddressLookupMode;
import com.google.gwt.search.client.LocalResult;
import com.google.gwt.search.client.LocalSearch;
import com.google.gwt.search.client.Result;
import com.google.gwt.search.client.SearchCompleteHandler;
import com.google.gwt.search.client.SearchCompleteHandler.SearchCompleteEvent;
import com.google.gwt.user.client.Timer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class PlaceSearch {

  private static final PlaceComparator _comparator = new PlaceComparator();

  private double _mergeDistance = 25.0;

  public void query(String query, PlaceSearchListener listener) {
    query(query, listener, null);
  }

  /**
   * Results with
   * 
   * @param mergeDistance in meters
   */
  public void setMergeDistance(double mergeDistance) {
    _mergeDistance = mergeDistance;
  }

  public void query(String query, PlaceSearchListener listener,
      LatLngBounds view) {

    final LocationHandler handler = new LocationHandler(listener, view);

    // Google Local Search
    LocalSearch search = new LocalSearch();

    search.setAddressLookupMode(AddressLookupMode.ENABLED);
    if (view != null)
      search.setCenterPoint(view.getCenter());
    search.addSearchCompleteHandler(handler);
    search.execute(query);

    // Google Maps Geocoder Search
    Geocoder geocoder = new Geocoder();
    if (view != null)
      geocoder.setViewport(view);
    geocoder.getLocations(query, handler);

    handler.scheduleRepeating(1000);
  }

  public static List<Place> getSeachCompleteEventAsPlaces(
      SearchCompleteEvent event, List<Place> places) {

    JsArray<? extends Result> results = event.getSearch().getResults();

    for (int i = 0; i < results.length(); i++) {
      Result result = results.get(i);
      if (result instanceof LocalResult) {
        LocalResult lsr = (LocalResult) result;
        places.add(new LocalResultPlaceImpl(lsr));
      }
    }

    return places;
  }

  public static List<Place> mergeNearbyEntries(List<Place> places, double mergeDistance) {

    TreeUnionFind<Place> unionFind = new TreeUnionFind<Place>();

    for (int i = 0; i < places.size(); i++) {

      Place place = places.get(i);
      unionFind.find(place);

      for (int j = i + 1; j < places.size(); j++) {
        Place other = places.get(j);
        double distance = place.getLocation().distanceFrom(other.getLocation());
        if (distance < mergeDistance)
          unionFind.union(place, other);
      }
    }

    List<Place> reduced = new ArrayList<Place>();

    for (Set<Place> cluster : unionFind.getSetMembers()) {
      List<Place> go = new ArrayList<Place>(cluster);
      Collections.sort(go, _comparator);
      reduced.add(go.get(0));
    }

    return reduced;
  }

  private class LocationHandler extends Timer implements LocationCallback,
      SearchCompleteHandler {

    private List<Place> _places = new ArrayList<Place>();

    private int _count = 0;

    private boolean _flushed = false;

    private PlaceSearchListener _listener;

    private LatLngBounds _view;

    private boolean _seenFirstOnSearchComplete = false;

    public LocationHandler(PlaceSearchListener listener, LatLngBounds view) {
      _listener = listener;
      _view = view;
    }

    public void onSuccess(JsArray<Placemark> placemarks) {

      for (int i = 0; i < placemarks.length(); i++) {
        Placemark mark = placemarks.get(i);
        addPlace(new PlacemarkPlaceImpl(mark));
      }

      handleResult(false);
    }

    public void onFailure(int statusCode) {
      handleResult(true);
    }

    public void onSearchComplete(SearchCompleteEvent event) {

      if (_seenFirstOnSearchComplete)
        return;
      _seenFirstOnSearchComplete = true;

      List<Place> places = getSeachCompleteEventAsPlaces(event,
          new ArrayList<Place>());

      for (Place place : places)
        addPlace(place);

      handleResult(false);
    }

    private void addPlace(Place place) {

      if (place.getName() == null)
        return;

      if (_view != null && !_view.containsLatLng(place.getLocation()))
        return;

      _places.add(place);
    }

    private void handleResult(boolean isError) {
      _count++;

      if (_count >= 2)
        flush();
    }

    public void flush() {

      if (_flushed)
        return;

      _flushed = true;

      if (_mergeDistance > 0)
        _places = mergeNearbyEntries(_places,_mergeDistance);

      if (_places.isEmpty()) {
        _listener.handleNoResult();
      } else if (_places.size() == 1) {
        _listener.handleSingleResult(_places.get(0));
      } else {
        _listener.handleMultipleResults(_places);
      }
    }

    /****
     * {@link Timer} Interface
     ****/

    @Override
    public void run() {
      if (_count >= 2 || (_count > 0 && _places.size() > 0)) {
        this.cancel();
        flush();
      }
    }

  }

  private static class PlaceComparator implements Comparator<Place> {

    public int compare(Place o1, Place o2) {

      boolean placemarkA = o1 instanceof PlacemarkPlaceImpl;
      boolean placemarkB = o2 instanceof PlacemarkPlaceImpl;

      if (placemarkA && !placemarkB)
        return -1;

      if (placemarkB && !placemarkA)
        return 1;

      int accuracyA = o1.getAccuracy();
      int accuracyB = o2.getAccuracy();

      if (accuracyA < accuracyB)
        return -1;
      if (accuracyB < accuracyA)
        return 1;

      return 0;
    }
  }
}
