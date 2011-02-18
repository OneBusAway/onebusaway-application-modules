package org.onebusaway.api.model.transit.tripplanning;

import java.io.Serializable;
import java.util.Map;

public class EdgeV2Bean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String fromId;

  private String toId;

  private String name;

  private String path;

  private Map<String, String> tags;

  public String getFromId() {
    return fromId;
  }

  public void setFromId(String fromId) {
    this.fromId = fromId;
  }

  public String getToId() {
    return toId;
  }

  public void setToId(String toId) {
    this.toId = toId;
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

  public Map<String, String> getTags() {
    return tags;
  }

  public void setTags(Map<String, String> tags) {
    this.tags = tags;
  }
}
