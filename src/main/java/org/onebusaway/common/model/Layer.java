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
package org.onebusaway.common.model;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "layers")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class Layer implements Comparable<Layer> {

  private static final long serialVersionUID = 1L;

  @Id
  private String name;

  private int sequence;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getSequence() {
    return sequence;
  }

  public void setSequence(int sequence) {
    this.sequence = sequence;
  }

  /***************************************************************************
   * {@link Comparable} Interface
   **************************************************************************/

  public int compareTo(Layer o) {
    if (sequence == o.sequence)
      return name.compareTo(o.name);
    return sequence < o.sequence ? -1 : 1;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof Layer))
      return false;
    Layer layer = (Layer) obj;
    return this.name.equals(layer.name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public String toString() {
    return this.name;
  }  
}
