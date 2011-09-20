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
package org.onebusaway.transit_data.model.oba;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class LocalSearchResult implements Serializable {

  private static final long serialVersionUID = 1L;

  private String id;

  private String name;

  private String url;

  private String address;

  private String city;

  private String region;

  private String country;

  private String zip;

  private String phoneNumber;

  private double lat;

  private double lon;

  private double rating;

  private String ratingUrl;

  private String ratingUrlSmall;

  private double maxRating;

  private Set<String> categories = new HashSet<String>();

  public LocalSearchResult() {

  }

  public LocalSearchResult(LocalSearchResult result) {
    id = result.id;
    name = result.name;
    url = result.url;
    address = result.address;
    city = result.city;
    region = result.region;
    country = result.country;
    phoneNumber = result.phoneNumber;
    lat = result.lat;
    lon = result.lon;
    rating = result.rating;
    maxRating = result.maxRating;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public double getLat() {
    return lat;
  }

  public void setLat(double lat) {
    this.lat = lat;
  }

  public double getLon() {
    return lon;
  }

  public void setLon(double lon) {
    this.lon = lon;
  }

  public double getRating() {
    return rating;
  }

  public void setRating(double rating) {
    this.rating = rating;
  }

  public String getRatingUrl() {
    return ratingUrl;
  }

  public void setRatingUrl(String ratingUrl) {
    this.ratingUrl = ratingUrl;
  }

  public String getRatingUrlSmall() {
    return ratingUrlSmall;
  }

  public void setRatingUrlSmall(String ratingUrlSmall) {
    this.ratingUrlSmall = ratingUrlSmall;
  }

  public double getMaxRating() {
    return maxRating;
  }

  public void setMaxRating(double maxRating) {
    this.maxRating = maxRating;
  }

  public String getZip() {
    return zip;
  }

  public void setZip(String zip) {
    this.zip = zip;
  }

  public Set<String> getCategories() {
    return categories;
  }

  public void setCategories(Set<String> categories) {
    this.categories = categories;
  }
}
