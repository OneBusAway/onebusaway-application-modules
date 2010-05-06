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
package edu.washington.cs.rse.transit.common.model.aggregate;

import edu.washington.cs.rse.transit.common.model.IdentityBean;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "transit_layers")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class Layer extends IdentityBean implements Comparable<Layer> {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue
  private int id;

  private String name;

  private int sequence;

  public Layer() {

  }

  public Layer(String name, int index) {
    this.name = name;
    this.sequence = index;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public int getSequence() {
    return sequence;
  }

  /***************************************************************************
   * {@link Comparable} Interface
   **************************************************************************/

  public int compareTo(Layer o) {
    if (sequence == o.sequence)
      return name.compareTo(o.name);
    return sequence < o.sequence ? -1 : 1;
  }
}
