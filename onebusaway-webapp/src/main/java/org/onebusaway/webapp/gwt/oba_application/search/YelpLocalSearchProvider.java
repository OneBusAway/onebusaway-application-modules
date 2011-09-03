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
package org.onebusaway.webapp.gwt.oba_application.search;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.transit_data.model.oba.LocalSearchResult;
import org.onebusaway.webapp.gwt.common.rpc.JsonCallback;
import org.onebusaway.webapp.gwt.common.rpc.JsonLibrary;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.maps.client.geom.LatLng;

import java.util.ArrayList;
import java.util.List;

public class YelpLocalSearchProvider implements LocalSearchProvider {

  private static final String YELP_API_BUSINESS_REVIEW_SEARCH = "http://api.yelp.com/business_review_search";

  private final String _apiKey;

  private int _requestId = 0;

  private int _maxResultCount = 20;

  private int _timeout = 3000;

  public YelpLocalSearchProvider(String apiKey) {
    _apiKey = apiKey;
  }

  public void setMaxResultCount(int maxResultCount) {
    _maxResultCount = maxResultCount;
  }

  public void setTimeout(int timeout) {
    _timeout = timeout;
  }

  public void search(CoordinateBounds bounds, String query, String category,
      LocalSearchCallback callback) {

    StringBuilder url = buildQuery(query, category);
    url.append("&tl_lat=").append(bounds.getMaxLat());
    url.append("&tl_long=").append(bounds.getMinLon());
    url.append("&br_lat=").append(bounds.getMinLat());
    url.append("&br_long=").append(bounds.getMaxLon());
    performQuery(url, callback);
  }

  public void search(LatLng center, String query, String category,
      LocalSearchCallback callback) {
    StringBuilder url = buildQuery(query, category);
    url.append("&lat=").append(center.getLatitude());
    url.append("&long=").append(center.getLongitude());
    performQuery(url, callback);
  }

  public void searchByRadius(LatLng center, double radius, String query,
      String category, LocalSearchCallback callback) {

    StringBuilder url = buildQuery(query, category);
    url.append("&lat=").append(center.getLatitude());
    url.append("&long=").append(center.getLongitude());
    url.append("&radius=").append(radius);
    performQuery(url, callback);
  }

  private StringBuilder buildQuery(String query, String category) {
    StringBuilder url = new StringBuilder();
    url.append(YELP_API_BUSINESS_REVIEW_SEARCH);
    url.append("?term=").append(query);
    if (category != null && category.length() > 0)
      url.append("&category=").append(category);
    url.append("&num_biz_requested=" + _maxResultCount);
    url.append("&ywsid=").append(_apiKey);
    return url;
  }

  private void performQuery(StringBuilder url, LocalSearchCallback callback) {
    url.append("&callback=");
    JsonHandler handler = new JsonHandler(callback);
    JsonLibrary.getJson("yelp" + (_requestId++), url.toString(), handler,
        _timeout);
  }

  private static class JsonHandler implements JsonCallback {

    private LocalSearchCallback _handler;

    public JsonHandler(LocalSearchCallback handler) {
      _handler = handler;
    }

    public void onSuccess(JavaScriptObject jso) {
      try {
        JSONObject obj = new JSONObject(jso);
        checkStatusCode(obj);
        JSONArray businesses = JsonLibrary.getJsonArray(obj, "businesses");
        if (businesses == null)
          throw new YelpException("invalid businesses field in response");
        List<LocalSearchResult> results = new ArrayList<LocalSearchResult>(
            businesses.size());
        for (int i = 0; i < businesses.size(); i++) {
          LocalSearchResult result = parseResult(businesses.get(i));
          if (result != null)
            results.add(result);
        }
        _handler.onSuccess(results);
      } catch (YelpException ex) {
        _handler.onFailure(ex);
      }
    }

    public void onFailure() {
      _handler.onFailure(new YelpTimeoutException());
    }

    private void checkStatusCode(JSONObject obj) throws YelpException,
        YelpStatusException {
      JSONObject messages = JsonLibrary.getJsonObj(obj, "message");
      if (messages == null)
        throw new YelpException("invalid messages field in response");
      Double code = JsonLibrary.getJsonDouble(messages, "code");
      if (code == null)
        throw new YelpException("invalid code field in response");
      if (code.intValue() != 0) {
        String message = "";
        String text = JsonLibrary.getJsonString(messages, "text");
        if (text != null)
          message = text;
        throw new YelpStatusException(message, code.intValue());
      }
    }

    private LocalSearchResult parseResult(JSONValue value) throws YelpException {

      JSONObject obj = value.isObject();
      if (obj == null)
        throw new YelpException("invalid business result");

      String id = JsonLibrary.getJsonString(obj, "id");
      if (id == null)
        throw new YelpException("invalid id field for business object");

      String name = JsonLibrary.getJsonString(obj, "name");
      if (name == null)
        throw new YelpException("invalid name field for business object");

      Double lat = JsonLibrary.getJsonDouble(obj, "latitude");
      Double lon = JsonLibrary.getJsonDouble(obj, "longitude");
      if (lat == null || lon == null)
        return null;

      Double rating = JsonLibrary.getJsonDouble(obj, "avg_rating");
      if (rating == null)
        throw new YelpException("invalid avg_rating field for business object");

      String url = JsonLibrary.getJsonString(obj, "url");
      String address = JsonLibrary.getJsonString(obj, "address1");
      String city = JsonLibrary.getJsonString(obj, "city");
      String region = JsonLibrary.getJsonString(obj, "state");
      String zip = JsonLibrary.getJsonString(obj, "zip");
      String phone = JsonLibrary.getJsonString(obj, "phone");

      String ratingUrl = JsonLibrary.getJsonString(obj, "rating_img_url");
      String ratingUrlSmall = JsonLibrary.getJsonString(obj,
          "rating_img_url_small");

      LocalSearchResult result = new LocalSearchResult();
      result.setId(id);
      result.setName(name);
      result.setUrl(url);
      result.setLat(lat);
      result.setLon(lon);
      result.setRating(rating);
      result.setMaxRating(5.0);
      result.setAddress(address);
      result.setCity(city);
      result.setRegion(region);
      result.setZip(zip);
      result.setPhoneNumber(phone);
      result.setRatingUrl(ratingUrl);
      result.setRatingUrlSmall(ratingUrlSmall);

      JSONArray categories = JsonLibrary.getJsonArray(obj, "categories");
      for (int i = 0; i < categories.size(); i++) {
        JSONValue cValue = categories.get(i);
        JSONObject cObject = cValue.isObject();
        String cName = JsonLibrary.getJsonString(cObject, "name");
        result.getCategories().add(cName);
      }

      return result;
    }
  }

}
