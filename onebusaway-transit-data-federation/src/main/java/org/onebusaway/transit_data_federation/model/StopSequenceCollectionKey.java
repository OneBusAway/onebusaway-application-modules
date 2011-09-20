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
package org.onebusaway.transit_data_federation.model;

import java.io.Serializable;

import org.onebusaway.gtfs.model.Route;

/**
 * Uniquely identifies a {@link StopSequenceCollection}
 * 
 * @author bdferris
 * 
 */
public class StopSequenceCollectionKey implements Serializable {

  private static final long serialVersionUID = 1L;

  private Route route;

  private String id;

  public StopSequenceCollectionKey() {

  }

  public StopSequenceCollectionKey(Route route, String id) {
    this.route = route;
    this.id = id;
  }

  public Route getRoute() {
    return route;
  }

  public void setRoute(Route route) {
    this.route = route;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof StopSequenceCollectionKey))
      return false;

    StopSequenceCollectionKey spbk = (StopSequenceCollectionKey) obj;
    return this.route.equals(spbk.route) && this.id.equals(spbk.id);
  }

  @Override
  public int hashCode() {
    return route.hashCode() * 7 + id.hashCode() * 13;
  }

  @Override
  public String toString() {
    return route + " " + id;
  }
}
