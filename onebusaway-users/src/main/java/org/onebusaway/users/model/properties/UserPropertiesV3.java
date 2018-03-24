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
package org.onebusaway.users.model.properties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.users.model.UserProperties;

/**
 * DO NO RENAME OR MOVE THIS CLASS. Serialized instances of this class will be
 * around forever. See notes in {@link UserProperties} for more info.
 * 
 * @author bdferris
 * @see UserProperties
 */
public class UserPropertiesV3 implements Serializable, UserProperties {

  /**
   * Don't EVER change this. See notes in {@link UserProperties}.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Should we remember preferences for the given user? Set to false if the user
   * wants to disable preferences.
   */
  private boolean rememberPreferencesEnabled = true;

  private String defaultLocationName;

  private double defaultLocationLat = Double.NaN;

  private double defaultLocationLon = Double.NaN;

  private List<Bookmark> bookmarks = new ArrayList<Bookmark>();

  private Long minApiRequestInterval = null;

  private Map<String, Long> readSituationIdsWithReadTime = new HashMap<String, Long>();
  
  // For version 3, add API Key contact info.
  private String contactName;
  
  private String contactCompany;
  
  private String contactEmail;
  
  private String contactDetails;

  public UserPropertiesV3() {

  }

  public UserPropertiesV3(UserPropertiesV3 o) {
    this.rememberPreferencesEnabled = o.rememberPreferencesEnabled;
    this.bookmarks = new ArrayList<Bookmark>(o.bookmarks);
    this.defaultLocationLat = o.defaultLocationLat;
    this.defaultLocationLon = o.defaultLocationLon;
    this.defaultLocationName = o.defaultLocationName;
    this.minApiRequestInterval = o.minApiRequestInterval;
    this.contactName = o.contactName;
    this.contactCompany = o.contactCompany;
    this.contactEmail = o.contactEmail;
    this.contactDetails = o.contactDetails;
  }

  public boolean isRememberPreferencesEnabled() {
    return rememberPreferencesEnabled;
  }

  public void setRememberPreferencesEnabled(boolean rememberPreferencesEnabled) {
    this.rememberPreferencesEnabled = rememberPreferencesEnabled;
  }

  public String getDefaultLocationName() {
    return defaultLocationName;
  }

  public void setDefaultLocationName(String defaultLocationName) {
    this.defaultLocationName = defaultLocationName;
  }

  public boolean hasDefaultLocationLat() {
    return !Double.isNaN(defaultLocationLat);
  }

  public double getDefaultLocationLat() {
    return defaultLocationLat;
  }

  public void setDefaultLocationLat(double defaultLocationLat) {
    this.defaultLocationLat = defaultLocationLat;
  }

  public boolean hasDefaultLocationLon() {
    return !Double.isNaN(defaultLocationLon);
  }

  public double getDefaultLocationLon() {
    return defaultLocationLon;
  }

  public void setDefaultLocationLon(double defaultLocationLon) {
    this.defaultLocationLon = defaultLocationLon;
  }

  public List<Bookmark> getBookmarks() {
    return bookmarks;
  }

  public void setBookmarks(List<Bookmark> bookmarks) {
    this.bookmarks = bookmarks;
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

  /**
   * API request throttling information
   * 
   * @return the minimum interval time (in ms) between successive API requests
   *         for this account.
   */
  public Long getMinApiRequestInterval() {
    return minApiRequestInterval;
  }

  /**
   * API request throttling information
   * 
   * @param minApiRequestInterval minimum time interval (in ms)
   */
  public void setMinApiRequestInterval(Long minApiRequestInterval) {
    this.minApiRequestInterval = minApiRequestInterval;
  }

  /**
   * Information about when a service alert situation id was read by the user.
   * 
   * @return a map from situation id to the time it was read (unix-time)
   */
  public Map<String, Long> getReadSituationIdsWithReadTime() {
    return readSituationIdsWithReadTime;
  }

  /**
   * Information about when a service alert situation id was read by the user.
   * 
   * @param a map from situation id to the time it was read (unix-time)
   */
  public void setReadSituationIdsWithReadTime(
      Map<String, Long> readSituationIdsWithReadTime) {
    this.readSituationIdsWithReadTime = readSituationIdsWithReadTime;
  }

  public void clear() {
    this.bookmarks = new ArrayList<Bookmark>();
    this.defaultLocationLat = Double.NaN;
    this.defaultLocationLon = Double.NaN;
    this.defaultLocationName = null;
    this.minApiRequestInterval = null;
    this.readSituationIdsWithReadTime = null;
    this.contactName = null;
    this.contactCompany = null;
    this.contactEmail = null;
    this.contactDetails = null;
  }
}
