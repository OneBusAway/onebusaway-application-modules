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
package org.onebusaway.where.model;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.onebusaway.common.model.IdentityBean;
import org.onebusaway.common.model.Region;
import org.onebusaway.gtfs.model.Stop;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "where_stop_regions")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@AccessType("field")
@org.hibernate.annotations.Entity(mutable = false)
public class StopRegion extends IdentityBean<Integer> {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue
  @AccessType("property")
  private int id;

  @ManyToOne
  private Stop stop;

  @ManyToOne
  private Region region;

  public StopRegion() {

  }

  public StopRegion(Stop stop, Region region) {
    this.stop = stop;
    this.region = region;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Stop getStop() {
    return stop;
  }

  public void setStop(Stop stop) {
    this.stop = stop;
  }

  public Region getRegion() {
    return region;
  }

  public void setRegion(Region region) {
    this.region = region;
  }
}
