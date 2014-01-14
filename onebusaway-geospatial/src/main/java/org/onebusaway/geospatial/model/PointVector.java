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
/**
 * 
 */
package org.onebusaway.geospatial.model;

import java.util.Arrays;

public class PointVector {

  private double[] _vector;

  public static <T extends Point> PointVector create(T origin, T direction) {
    double[] vector = new double[origin.getDimensions()];
    for (int i = 0; i < vector.length; i++)
      vector[i] = direction.getOrdinate(i) - origin.getOrdinate(i);
    return new PointVector(vector);
  }

  public PointVector(double... vector) {
    _vector = vector;
  }

  public int getDimensions() {
    return _vector.length;
  }

  public double getOrdinate(int dimension) {
    return _vector[dimension];
  }

  public double[] getOrdinates() {
    double[] ord = new double[_vector.length];
    System.arraycopy(_vector, 0, ord, 0, ord.length);
    return ord;
  }

  public double getX() {
    return _vector[0];
  }

  public double getY() {
    return _vector[1];
  }

  public double getZ() {
    return _vector[2];
  }

  /*******************************************************************************************************************
   * Properties
   ******************************************************************************************************************/

  public double length() {
    double sum = 0.0;
    for (int i = 0; i < _vector.length; i++)
      sum += _vector[i] * _vector[i];
    return Math.sqrt(sum);
  }

  public double dotProduct(PointVector v) {
    double sum = 0.0;
    for (int i = 0; i < _vector.length; i++)
      sum += _vector[i] * v.getOrdinate(i);
    return sum;
  }

  public PointVector getCrossProduct(PointVector v) {

    if (getDimensions() != 3)
      throw new IllegalStateException("We only handle 3D case at the moment");

    double[] a = _vector;
    double[] b = v._vector;

    double x = a[1] * b[2] - a[2] * b[1];
    double y = a[2] * b[0] - a[0] * b[2];
    double z = a[0] * b[1] - a[1] * b[0];

    return new PointVector(x, y, z);
  }

  public double getCosAngle(PointVector v) {
    return dotProduct(v) / (length() * v.length());
  }

  public double getSinAngle(PointVector v) {
    PointVector xp = getCrossProduct(v);
    double len = xp.length();
    len /= (length() * v.length());
    return len;
  }

  /**
   * 
   * @param v the other vector
   * @return the angle between two vectors
   */
  public double getAngle(PointVector v) {
    double cosTheta = getCosAngle(v);
    return Math.acos(cosTheta);
  }

  public double getAngle() {
    return Math.atan2(getY(), getX());
  }

  /*******************************************************************************************************************
   * Operations
   ******************************************************************************************************************/

  public PointVector getScaled(double factor) {
    double[] ord = getOrdinates();
    for (int i = 0; i < ord.length; i++)
      ord[i] *= factor;
    return new PointVector(ord);
  }

  public PointVector getUnitVector() {
    return getAsLength(1.0);
  }

  /**
   * Project a vector v onto this vector
   * 
   * @param v the vector to project
   * @return the projection of v onto this vector
   */
  public PointVector getProjection(PointVector v) {
    double dp = dotProduct(v) / length();
    return getAsLength(dp);
  }

  /**
   * Scale our vector such that it has the specified length
   * 
   * @param length - the target length
   * @return a vector in the same direction but with the specified length
   */
  public PointVector getAsLength(double length) {
    return getScaled(length / length());
  }

  public PointVector rotate(double angle) {
    return rotate(angle, 0, 1);
  }

  public PointVector rotate(double angle, int dimensionA, int dimensionB) {
    double[] ord = getOrdinates();
    ord[dimensionA] = _vector[dimensionA] * Math.cos(angle)
        + _vector[dimensionB] * Math.sin(angle);
    ord[dimensionB] = _vector[dimensionB] * Math.cos(angle)
        - _vector[dimensionA] * Math.sin(angle);
    return new PointVector(ord);
  }

  /**
   * 
   * @param <T> the position type
   * @param origin point to translate
   * @return a new position equal to the specified shifted by the vector
   */
  @SuppressWarnings("unchecked")
  public <T extends Point> T addToPoint(T origin) {
    return (T) origin.translate(_vector);
  }

  /*******************************************************************************************************************
   * {@link Object} Interface
   ******************************************************************************************************************/

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof PointVector))
      return false;

    PointVector v = (PointVector) obj;
    return Arrays.equals(_vector, v._vector);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(_vector);
  }

  @Override
  public String toString() {
    return Arrays.toString(_vector);
  }

}