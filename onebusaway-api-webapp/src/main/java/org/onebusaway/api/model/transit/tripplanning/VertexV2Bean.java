package org.onebusaway.api.model.transit.tripplanning;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.onebusaway.geospatial.model.CoordinatePoint;

public class VertexV2Bean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String id;

  private CoordinatePoint location;

  private Map<String, String> tags;

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

  public Map<String, String> getTags() {
    return tags;
  }

  public void setTags(Map<String, String> tags) {
    this.tags = tags;
  }
}
