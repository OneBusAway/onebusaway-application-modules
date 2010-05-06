package org.onebusaway.users.client.model;

import java.io.Serializable;
import java.util.List;

public class UserBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private boolean anonymous;

  private boolean defaultLocation = false;

  private String defaultLocationName;

  private double defautLocationLat;

  private double defaultLocationLon;

  private String lastSelectedStopId;

  private List<String> bookmarkedStopIds;

  public boolean isAnonymous() {
    return anonymous;
  }

  public void setAnonymous(boolean anonymous) {
    this.anonymous = anonymous;
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

  public double getDefautLocationLat() {
    return defautLocationLat;
  }

  public void setDefautLocationLat(double defautLocationLat) {
    this.defautLocationLat = defautLocationLat;
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
}
