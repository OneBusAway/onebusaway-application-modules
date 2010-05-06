/*
 * Copyright 2008 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.gtdf.model;

import org.onebusaway.csv.CsvField;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class ShapePointKey implements Serializable, Comparable<ShapePointKey> {

  private static final long serialVersionUID = 1L;

  private String shapeId;

  @CsvField(name = "shape_pt_sequence")
  private int sequence;

  public ShapePointKey() {

  }

  public ShapePointKey(String shapeId, int sequence) {
    this.shapeId = shapeId;
    this.sequence = sequence;
  }

  public String getShapeId() {
    return this.shapeId;
  }

  public void setShapeId(String shapeId) {
    this.shapeId = shapeId;
  }

  public int getSequence() {
    return this.sequence;
  }

  public void setSequence(int sequence) {
    this.sequence = sequence;
  }

  public int compareTo(ShapePointKey o) {
    if (this.shapeId == o.shapeId)
      return this.sequence == o.sequence ? 0 : (this.sequence < o.sequence ? -1
          : 1);
    return this.shapeId.compareTo(o.shapeId);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof ShapePointKey))
      return false;

    ShapePointKey spk = (ShapePointKey) obj;
    return this.shapeId == spk.shapeId && this.sequence == spk.sequence;
  }

  @Override
  public int hashCode() {
    return 5 * this.shapeId.hashCode() + 7 * this.sequence;
  }

  @Override
  public String toString() {
    return this.shapeId + " " + this.sequence;
  }

}
