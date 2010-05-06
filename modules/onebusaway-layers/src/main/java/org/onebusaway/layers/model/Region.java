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
package org.onebusaway.layers.model;

import org.onebusaway.container.model.IdentityBean;

import com.vividsolutions.jts.geom.Geometry;

import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "regions")
public class Region extends IdentityBean<Integer> implements Comparable<Region> {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue
  private Integer id;

  private String name;

  @Type(type = "org.hibernatespatial.GeometryUserType")
  @Column(columnDefinition = "GEOMETRY")
  private Geometry boundary;

  @ManyToOne
  private Layer layer;

  public Region() {

  }

  public Region(String name, Geometry geometry) {
    setName(name);
    setBoundary(geometry);
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public Geometry getBoundary() {
    return boundary;
  }

  public void setBoundary(Geometry geometry) {
    boundary = geometry;
  }

  public Layer getLayer() {
    return layer;
  }

  public void setLayer(Layer layer) {
    this.layer = layer;
  }

  public int compareTo(Region o) {
    return name.compareTo(o.getName());
  }

  @Override
  public String toString() {
    return this.name;
  }
}
