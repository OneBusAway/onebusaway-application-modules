package org.onebusaway.common.web.common.client.control;

import com.google.gwt.maps.client.geocode.Placemark;
import com.google.gwt.maps.client.geom.LatLng;

import java.util.List;

public interface GeocoderResultListener {
  public void setQueryLocation(LatLng location);

  public void setNoQueryLocations();

  public void setTooManyQueryLocations(List<Placemark> locations);

  public void setErrorOnQueryLocation();
}
