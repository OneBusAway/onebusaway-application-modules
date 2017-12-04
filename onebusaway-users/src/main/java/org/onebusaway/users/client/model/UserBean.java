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
package org.onebusaway.users.client.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String userId;

  private boolean rememberPreferencesEnabled = true;

  private boolean defaultLocation = false;

  private String defaultLocationName;

  private double defaultLocationLat = Double.NaN;

  private double defaultLocationLon = Double.NaN;

  private List<String> lastSelectedStopIds = new ArrayList<String>();

  private List<BookmarkBean> bookmarks = new ArrayList<BookmarkBean>();

  private Long minApiRequestInterval = null;

  private Map<String, Long> readServiceAlerts = null;
  
  private String contactName;
  
  private String contactCompany;
  
  private String contactEmail;
  
  private String contactDetails;

  private boolean anonymous = true;

  private boolean admin = false;

  private boolean disabled = false;

  private List<UserIndexBean> indices = new ArrayList<UserIndexBean>();

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public boolean isRememberPreferencesEnabled() {
    return rememberPreferencesEnabled;
  }

  public void setRememberPreferencesEnabled(boolean rememberPreferencesEnabled) {
    this.rememberPreferencesEnabled = rememberPreferencesEnabled;
  }

  public boolean hasDefaultLocation() {
    return defaultLocation;
  }

  public void setHasDefaultLocation(boolean defaultLocation) {
    this.defaultLocation = defaultLocation;
  }

  public String getDefaultLocationName() {
    return defaultLocationName;
  }

  public void setDefaultLocationName(String defaultLocationName) {
    this.defaultLocationName = defaultLocationName;
  }

  public double getDefaultLocationLat() {
    return defaultLocationLat;
  }

  public void setDefaultLocationLat(double defaultLocationLat) {
    this.defaultLocationLat = defaultLocationLat;
  }

  public double getDefaultLocationLon() {
    return defaultLocationLon;
  }

  public void setDefaultLocationLon(double defaultLocationLon) {
    this.defaultLocationLon = defaultLocationLon;
  }

  public List<String> getLastSelectedStopIds() {
    return lastSelectedStopIds;
  }

  public void setLastSelectedStopIds(List<String> lastSelectedStopIds) {
    this.lastSelectedStopIds = lastSelectedStopIds;
  }

  public List<BookmarkBean> getBookmarks() {
    return bookmarks;
  }

  public void setBookmarks(List<BookmarkBean> bookmarks) {
    this.bookmarks = bookmarks;
  }

  public void addBookmark(BookmarkBean bookmark) {
    bookmarks.add(bookmark);
  }

  public Long getMinApiRequestInterval() {
    return minApiRequestInterval;
  }

  public void setMinApiRequestInterval(Long minApiRequestInterval) {
    this.minApiRequestInterval = minApiRequestInterval;
  }

  public Map<String, Long> getReadServiceAlerts() {
    return readServiceAlerts;
  }

  public void setReadServiceAlerts(Map<String, Long> readServiceAlerts) {
    this.readServiceAlerts = readServiceAlerts;
  }

  public boolean isAnonymous() {
    return anonymous;
  }

  public void setAnonymous(boolean anonymous) {
    this.anonymous = anonymous;
  }

  public boolean isAdmin() {
    return admin;
  }

  public void setAdmin(boolean admin) {
    this.admin = admin;
  }

  public List<UserIndexBean> getIndices() {
    return indices;
  }

  public void setIndices(List<UserIndexBean> indices) {
    this.indices = indices;
  }

  public String getContactName() {
    return contactName;
  }
  
  public void setContactName(String contactName) {
    this.contactName = contactName;
  }
  
  public String getContactCompany() {
    return contactCompany;
  }
  
  public void setContactCompany(String contactCompany) {
    this.contactCompany = contactCompany;
  }
  
  public String getContactEmail() {
    return contactEmail;
  }
  
  public void setContactEmail(String contactEmail) {
    this.contactEmail = contactEmail;
  }
  
  public String getContactDetails() {
    return contactDetails;
  }
  
  public void setContactDetails(String contactDetails) {
    this.contactDetails = contactDetails;
  }

  public boolean isDisabled() { return disabled; }

  public void setDisabled(boolean disabled) { this.disabled = disabled; }
}
