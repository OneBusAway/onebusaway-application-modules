package org.onebusaway.webapp.gwt.oba_application.control;

import org.onebusaway.transit_data.model.oba.OneBusAwayConstraintsBean;

import com.google.gwt.maps.client.geom.LatLng;

public interface OneBusAwayStandardControl extends CommonControl {

  public void query(String query, String locationQuery, LatLng location,
      OneBusAwayConstraintsBean constraints);

}
