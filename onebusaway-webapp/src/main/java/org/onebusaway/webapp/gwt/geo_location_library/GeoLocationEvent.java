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
package org.onebusaway.webapp.gwt.geo_location_library;

import com.google.gwt.event.shared.GwtEvent;

public class GeoLocationEvent extends GwtEvent<GeoLocationHandler> {

  public static final Type<GeoLocationHandler> TYPE = new Type<GeoLocationHandler>();

  private double _timestamp;

  private double _lat;

  private double _lon;

  public GeoLocationEvent(double timestamp, double lat, double lon) {
    _timestamp = timestamp;
    _lat = lat;
    _lon = lon;
  }

  public double getTimestamp() {
    return _timestamp;
  }

  public double getLat() {
    return _lat;
  }

  public double getLon() {
    return _lon;
  }

  @Override
  protected void dispatch(GeoLocationHandler handler) {
    handler.handleLocation(this);
  }

  @Override
  public final com.google.gwt.event.shared.GwtEvent.Type<GeoLocationHandler> getAssociatedType() {
    return TYPE;
  }
}
