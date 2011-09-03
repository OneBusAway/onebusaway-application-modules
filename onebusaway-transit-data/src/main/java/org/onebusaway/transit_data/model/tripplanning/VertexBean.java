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
package org.onebusaway.transit_data.model.tripplanning;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.onebusaway.geospatial.model.CoordinatePoint;

public class VertexBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String id;

  private CoordinatePoint location;

  private List<EdgeNarrativeBean> incoming;

  private List<EdgeNarrativeBean> outgoing;

  private Map<String, Object> tags;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public CoordinatePoint getLocation() {
    return location;
  }

  public void setLocation(CoordinatePoint location) {
    this.location = location;
  }

  public List<EdgeNarrativeBean> getIncoming() {
    return incoming;
  }

  public void setIncoming(List<EdgeNarrativeBean> incoming) {
    this.incoming = incoming;
  }

  public List<EdgeNarrativeBean> getOutgoing() {
    return outgoing;
  }

  public void setOutgoing(List<EdgeNarrativeBean> outgoing) {
    this.outgoing = outgoing;
  }

  public Map<String, Object> getTags() {
    return tags;
  }

  public void setTags(Map<String, Object> tags) {
    this.tags = tags;
  }
}
