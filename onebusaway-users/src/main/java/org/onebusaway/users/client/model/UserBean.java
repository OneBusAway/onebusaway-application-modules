package org.onebusaway.users.client.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String userId;

  private boolean rememberPreferencesEnabled = true;

  private boolean defaultLocation = false;

  private String defaultLocationName;

  private double defaultLocationLat;

  private double defaultLocationLon;

  private List<String> lastSelectedStopIds = new ArrayList<String>();

  private List<BookmarkBean> bookmarks = new ArrayList<BookmarkBean>();

  private boolean anonymous;

  private boolean admin;

  private List<UserIndexBean> indices;

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
}
