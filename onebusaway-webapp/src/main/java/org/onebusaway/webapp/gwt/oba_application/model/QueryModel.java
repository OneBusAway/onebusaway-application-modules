package org.onebusaway.webapp.gwt.oba_application.model;

import org.onebusaway.transit_data.model.oba.OneBusAwayConstraintsBean;
import org.onebusaway.webapp.gwt.common.model.ModelEventSink;

import com.google.gwt.maps.client.geom.LatLng;

public class QueryModel extends LocationQueryModel {

  private ModelEventSink<QueryModel> _eventSink;

  private String _query;

  public void setQueryModelEventSink(ModelEventSink<QueryModel> eventSink) {
    _eventSink = eventSink;
  }

  public String getQuery() {
    return _query;
  }

  public void setQuery(String query, String locationQuery, LatLng location,
      OneBusAwayConstraintsBean constraints) {
    _query = query;
    setQuery(locationQuery, location, constraints);
  }

  @Override
  protected void fireModelChanged() {

    super.fireModelChanged();

    if (_eventSink != null)
      _eventSink.fireModelChange(this);
  }
}
