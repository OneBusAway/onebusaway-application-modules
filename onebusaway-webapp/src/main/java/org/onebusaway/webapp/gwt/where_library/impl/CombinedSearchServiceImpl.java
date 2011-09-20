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
package org.onebusaway.webapp.gwt.where_library.impl;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.transit_data.model.SearchQueryBean;
import org.onebusaway.webapp.gwt.where_library.rpc.WebappServiceAsync;
import org.onebusaway.webapp.gwt.where_library.services.CombinedSearchResult;
import org.onebusaway.webapp.gwt.where_library.services.CombinedSearchService;

import com.google.gwt.maps.client.geocode.Geocoder;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.search.client.AddressLookupMode;
import com.google.gwt.search.client.LocalSearch;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class CombinedSearchServiceImpl implements CombinedSearchService {

  private static WebappServiceAsync _service = WebappServiceAsync.SERVICE;

  @Override
  public void search(SearchQueryBean query, int placeTimeoutMillis,
      AsyncCallback<CombinedSearchResult> callback) {

    LatLngBounds originalBounds = getBounds(query.getBounds());
    System.out.println(query.getBounds());
    System.out.println(originalBounds);

    LatLngBounds smallerBounds = null;
    if (originalBounds != null) {
      LatLng center = originalBounds.getCenter();
      CoordinateBounds cb2 = SphericalGeometryLibrary.bounds(
          center.getLatitude(), center.getLongitude(), 4000);
      smallerBounds = getBounds(cb2);
      System.out.println(cb2);
      System.out.println(smallerBounds);
    }

    CombinedSearchHandlerImpl handler = new CombinedSearchHandlerImpl(
        originalBounds, placeTimeoutMillis, callback);

    // Start the route and stop search
    _service.getRoutesAndStops(query, handler);

    // Google Maps Geocoder Search
    Geocoder geocoder = new Geocoder();
    if (smallerBounds != null)
      geocoder.setViewport(smallerBounds);
    geocoder.getLocations(query.getQuery(), handler);

    // Google Local Search
    LocalSearch search = new LocalSearch();
    search.setAddressLookupMode(AddressLookupMode.ENABLED);
    if (!smallerBounds.isEmpty())
      search.setCenterPoint(smallerBounds.getCenter());
    search.addSearchCompleteHandler(handler);
    search.execute(query.getQuery());
  }

  private LatLngBounds getBounds(CoordinateBounds bounds) {

    LatLngBounds b = LatLngBounds.newInstance();

    if (bounds != null) {
      b.extend(LatLng.newInstance(bounds.getMinLat(), bounds.getMinLon()));
      b.extend(LatLng.newInstance(bounds.getMaxLat(), bounds.getMaxLon()));
    }

    return b;
  }
}
