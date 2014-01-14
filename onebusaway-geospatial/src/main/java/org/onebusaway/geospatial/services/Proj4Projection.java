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
package org.onebusaway.geospatial.services;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.model.XYPoint;

import com.jhlabs.map.proj.Projection;
import com.jhlabs.map.proj.ProjectionFactory;

public class Proj4Projection implements ICoordinateProjection, Serializable {

  private static final long serialVersionUID = 1L;

  private String[] _spec;

  private transient Projection _projection;

  public Proj4Projection(String... spec) {
    _spec = spec;
    populateProjection();
  }

  public String[] getSpec() {
    return _spec;
  }

  public XYPoint forward(CoordinatePoint point) {
    Point2D.Double from = new Point2D.Double(point.getLon(), point.getLat());
    Point2D.Double result = new Point2D.Double();
    result = _projection.transform(from, result);
    return new XYPoint(result.x, result.y);
  }

  public <T extends Collection<XYPoint>> T forward(
      Iterable<CoordinatePoint> source, T dest, int size) {
    for (CoordinatePoint p : source)
      dest.add(forward(p));
    return dest;
  }

  public CoordinatePoint reverse(XYPoint point) {
    Point2D.Double from = new Point2D.Double(point.getX(), point.getY());
    Point2D.Double result = new Point2D.Double();
    _projection.inverseTransform(from, result);
    return new CoordinatePoint(result.y, result.x);
  }

  public <T extends Collection<CoordinatePoint>> T reverse(
      Iterable<XYPoint> source, T dest, int size) {
    for (XYPoint p : source)
      dest.add(reverse(p));
    return dest;
  }

  /***************************************************************************
   * {@link Object} Interface
   **************************************************************************/

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof Proj4Projection))
      return false;
    Proj4Projection proj = (Proj4Projection) obj;
    return Arrays.equals(this._spec, proj._spec);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(_spec);
  }

  /***************************************************************************
   * Serialization Support
   **************************************************************************/

  private void populateProjection() {
    _projection = ProjectionFactory.fromPROJ4Specification(_spec);
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.writeObject(_spec);
  }

  private void readObject(ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    _spec = (String[]) in.readObject();
    populateProjection();
  }
}
