package org.onebusaway.users.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserPropertiesV1 implements Serializable, UserProperties {

  private static final long serialVersionUID = 1L;

  /**
   * Should we remember preferences for the given user? Set to false if the user
   * wants to disable preferences.
   */
  private boolean rememberPreferencesEnabled = true;

  private String defaultLocationName;

  private double defaultLocationLat = Double.NaN;

  private double defaultLocationLon = Double.NaN;

  private String lastSelectedStopId;

  private List<String> bookmarkedStopIds = new ArrayList<String>();

  public UserPropertiesV1() {

  }

  public UserPropertiesV1(UserPropertiesV1 o) {
    this.rememberPreferencesEnabled = o.rememberPreferencesEnabled;
    this.bookmarkedStopIds = new ArrayList<String>(o.bookmarkedStopIds);
    this.defaultLocationLat = o.defaultLocationLat;
    this.defaultLocationLon = o.defaultLocationLon;
    this.defaultLocationName = o.defaultLocationName;
    this.lastSelectedStopId = o.lastSelectedStopId;
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

  public String getLastSelectedStopId() {
    return lastSelectedStopId;
  }

  public void setLastSelectedStopId(String lastSelectedStopId) {
    this.lastSelectedStopId = lastSelectedStopId;
  }

  public List<String> getBookmarkedStopIds() {
    return bookmarkedStopIds;
  }

  public void setBookmarkedStopIds(List<String> bookmarkedStopIds) {
    this.bookmarkedStopIds = bookmarkedStopIds;
  }

  public void clear() {
    this.bookmarkedStopIds = new ArrayList<String>();
    this.defaultLocationLat = Double.NaN;
    this.defaultLocationLon = Double.NaN;
    this.defaultLocationName = null;
    this.lastSelectedStopId = null;
  }

  @Override
  public UserProperties deepCopy() {
    return new UserPropertiesV1(this);
  }
}
