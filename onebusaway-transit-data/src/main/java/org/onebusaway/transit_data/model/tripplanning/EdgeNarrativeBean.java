package org.onebusaway.transit_data.model.tripplanning;

import java.io.Serializable;
import java.util.Map;

public class EdgeNarrativeBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private VertexBean from;

  private VertexBean to;

  private String name;
  
  private String path;

  private Map<String, Object> tags;

  public VertexBean getFrom() {
    return from;
  }

  public void setFrom(VertexBean from) {
    this.from = from;
  }

  public VertexBean getTo() {
    return to;
  }

  public void setTo(VertexBean to) {
    this.to = to;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public Map<String, Object> getTags() {
    return tags;
  }

  public void setTags(Map<String, Object> tags) {
    this.tags = tags;
  }
}
