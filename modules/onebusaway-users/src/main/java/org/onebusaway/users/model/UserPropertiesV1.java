package org.onebusaway.users.model;

import java.io.Serializable;
import java.util.List;

public class UserPropertiesV1 implements Serializable {

  private static final long serialVersionUID = 1L;

  private String defaultLocationName;

  private boolean defaultLocationLatSet = false;

  private double defaultLocationLat;

  private boolean defaultLocationLonSet = false;

  private double defaultLocationLon;

  private String lastSelectedStopId;

  private List<String> bookmarkedStopIds;

  public String getDefaultLocationName() {
    return defaultLocationName;
  }

  public void setDefaultLocationName(String defaultLocationName) {
    this.defaultLocationName = defaultLocationName;
  }

  public boolean hasDefaultLocationLat() {
    return defaultLocationLatSet;
  }

  public double getDefaultLocationLat() {
    return defaultLocationLat;
  }

  public void setDefaultLocationLat(double defaultLocationLat) {
    this.defaultLocationLat = defaultLocationLat;
    this.defaultLocationLatSet = true;
  }

  public boolean hasDefaultLocationLon() {
    return defaultLocationLonSet;
  }

  public double getDefaultLocationLon() {
    return defaultLocationLon;
  }

  public void setDefaultLocationLon(double defaultLocationLon) {
    this.defaultLocationLon = defaultLocationLon;
    this.defaultLocationLonSet = true;
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
