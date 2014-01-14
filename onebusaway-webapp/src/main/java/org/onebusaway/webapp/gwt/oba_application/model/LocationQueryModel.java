/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.webapp.gwt.oba_application.model;

import org.onebusaway.transit_data.model.tripplanning.TransitShedConstraintsBean;
import org.onebusaway.webapp.gwt.common.model.ModelEventSink;

import com.google.gwt.maps.client.geom.LatLng;

public class LocationQueryModel {

  private ModelEventSink<LocationQueryModel> _events;

  private LatLng _location;

  private String _locationQuery;

  private long _time;

  private TransitShedConstraintsBean _constraints;

  public void setEventSink(ModelEventSink<LocationQueryModel> events) {
    _events = events;
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
  
  public long getTime() {
    return _time;
  }

  public TransitShedConstraintsBean getConstraints() {
    return _constraints;
  }

  public void setQuery(String locationQuery, LatLng location, long time,
      TransitShedConstraintsBean constraints) {
    _locationQuery = locationQuery;
    _location = location;
    _time = time;
    _constraints = constraints;
    fireModelChanged();
  }

  public void setQueryLocation(LatLng location) {
    _location = location;
    fireModelChanged();
  }

  protected void fireModelChanged() {
    if (_events != null)
      _events.fireModelChange(this);
  }
}
