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
package org.onebusaway.transit_data_federation.impl.otp.graph;

import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.opentripplanner.routing.core.Vertex;

import com.vividsolutions.jts.geom.Coordinate;

public abstract class AbstractVertex implements Vertex, TransitVertex {

  protected final GraphContext _context;

  public AbstractVertex(GraphContext context) {
    _context = context;
  }

  public GraphContext getContext() {
    return _context;
  }

  @Override
  public String getLabel() {
    throw new UnsupportedOperationException();
  }

  @Override
  public AgencyAndId getStopId() {
    return null;
  }

  @Override
  public double distance(Vertex v) {
    return SphericalGeometryLibrary.distanceFaster(getY(), getX(), v.getY(),
        v.getX());
  }

  @Override
  public double distance(Coordinate c) {
    return SphericalGeometryLibrary.distance(getY(), getX(), c.y, c.x);
  }

  @Override
  public Coordinate getCoordinate() {
    return new Coordinate(getX(), getY());
  }

  @Override
  public String getName() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setDistanceToNearestTransitStop(double distance) {
    // TODO Auto-generated method stub

  }

  @Override
  public double getDistanceToNearestTransitStop() {
    // TODO Auto-generated method stub
    return 0;
  }
}