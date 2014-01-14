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
package org.onebusaway.api.model.transit.tripplanning;

import java.util.List;

public class GraphResultV2Bean {
  private List<VertexV2Bean> vertices;

  private List<EdgeV2Bean> edges;

  public List<VertexV2Bean> getVertices() {
    return vertices;
  }

  public void setVertices(List<VertexV2Bean> vertices) {
    this.vertices = vertices;
  }

  public List<EdgeV2Bean> getEdges() {
    return edges;
  }

  public void setEdges(List<EdgeV2Bean> edges) {
    this.edges = edges;
  }
}
