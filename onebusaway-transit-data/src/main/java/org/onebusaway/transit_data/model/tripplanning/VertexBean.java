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
