package org.onebusaway.users.model.properties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.onebusaway.users.model.UserProperties;

public class UserPropertiesV2 implements Serializable, UserProperties {

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

  public UserPropertiesV2() {

  }

  public UserPropertiesV2(UserPropertiesV2 o) {
    this.rememberPreferencesEnabled = o.rememberPreferencesEnabled;
    this.bookmarks = new ArrayList<Bookmark>(o.bookmarks);
    this.defaultLocationLat = o.defaultLocationLat;
    this.defaultLocationLon = o.defaultLocationLon;
    this.defaultLocationName = o.defaultLocationName;
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

  public void clear() {
    this.bookmarks = new ArrayList<Bookmark>();
    this.defaultLocationLat = Double.NaN;
    this.defaultLocationLon = Double.NaN;
    this.defaultLocationName = null;
  }
}
