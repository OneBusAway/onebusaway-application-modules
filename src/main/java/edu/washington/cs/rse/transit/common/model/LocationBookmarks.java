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
package edu.washington.cs.rse.transit.common.model;

import org.hibernate.annotations.IndexColumn;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "transit_location_bookmarks_to_stop")
public class LocationBookmarks extends EntityBean {

  private static final long serialVersionUID = 1L;

  @Id
  private String id;

  @ManyToMany(fetch = FetchType.EAGER)
  @IndexColumn(name = "bookmark_position", base = 1)
  private List<StopLocation> bookmarks = new ArrayList<StopLocation>();

  @ManyToOne
  private StopLocation lastSelection;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<StopLocation> getBookmarks() {
    return bookmarks;
  }

  public StopLocation getLastSelection() {
    return lastSelection;
  }

  public void setLastSelection(StopLocation lastSelection) {
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
