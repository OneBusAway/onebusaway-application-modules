package org.onebusaway.oba.web.standard.client.model;

import org.onebusaway.common.web.common.client.model.ModelEventSink;
import org.onebusaway.oba.web.common.client.model.OneBusAwayConstraintsBean;

import com.google.gwt.maps.client.geom.LatLng;

public class QueryModel {

  private ModelEventSink<QueryModel> _events;

  private String _query;

  private LatLng _location;

  private String _locationQuery;

  private OneBusAwayConstraintsBean _constraints;

  public void setEvents(ModelEventSink<QueryModel> events) {
    _events = events;
  }

  public String getQuery() {
    return _query;
  }

  public String getLocationQuery() {
    return _locationQuery;
  }

  public boolean hasLocation() {
    return _location != null;
  }

  public LatLng getLocation() {
    return _location;
  }

  public OneBusAwayConstraintsBean getConstraints() {
    return _constraints;
  }

  public void setQuery(String query, String locationQuery, OneBusAwayConstraintsBean constraints) {
    setQuery(query, locationQuery, null, constraints);
  }

  public void setQuery(String query, String locationQuery, LatLng location, OneBusAwayConstraintsBean constraints) {
    _query = query;
    _locationQuery = locationQuery;
    _location = location;
    _constraints = constraints;
    _events.fireModelChange(this);
  }

  public void setQueryLocation(LatLng location) {
    _location = location;
    _events.fireModelChange(this);
  }
}
