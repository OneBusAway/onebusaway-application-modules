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
package org.onebusaway.transit_data_federation.model;

import org.hibernate.annotations.IndexColumn;

import org.onebusaway.container.model.IdentityBean;
import org.onebusaway.gtfs.model.Stop;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "where_location_bookmarks")
public class LocationBookmarks extends IdentityBean<String> {

  private static final long serialVersionUID = 1L;

  @Id
  private String id;

  @ManyToMany(fetch = FetchType.EAGER)
  @IndexColumn(name = "bookmark_position", base = 1)
  private List<Stop> bookmarks = new ArrayList<Stop>();

  @ManyToOne
  private Stop lastSelection;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<Stop> getBookmarks() {
    return bookmarks;
  }

  public Stop getLastSelection() {
    return lastSelection;
  }

  public void setLastSelection(Stop lastSelection) {
    this.lastSelection = lastSelection;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof LocationBookmarks))
      return false;
    LocationBookmarks b = (LocationBookmarks) obj;
    return this.id.equals(b.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
