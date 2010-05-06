package org.onebusaway.webapp.gwt.common.control;

import com.google.gwt.maps.client.geom.LatLng;

import java.util.List;

public interface Place {
  public String getName();
  public List<String> getDescription();
  public String getDescriptionAsString();
  public LatLng getLocation();
  public int getAccuracy();
}
