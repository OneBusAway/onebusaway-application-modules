package org.onebusaway.presentation.model;


public class DefaultSearchLocation {

  private final String name;

  private final double lat;

  private final double lon;

  private final boolean temporary;

  public DefaultSearchLocation(String name, double lat, double lon,
      boolean temporary) {
    this.name = name;
    this.lat = lat;
    this.lon = lon;
    this.temporary = temporary;
  }

  public String getName() {
    return name;
  }

  public double getLat() {
    return lat;
  }

  public double getLon() {
    return lon;
  }

  public boolean isTemporary() {
    return temporary;
  }

}
