package org.onebusaway.transit_data.model;

import org.onebusaway.geospatial.model.CoordinateBounds;

import java.io.Serializable;

@QueryBean
public class SearchQueryBean implements Serializable {

  private static final long serialVersionUID = 1L;

  public enum EQueryType implements Serializable {
    BOUNDS, CLOSEST, BOUNDS_OR_CLOSEST
  }

  private EQueryType type;

  private CoordinateBounds bounds;

  private String query;

  private int maxCount;

  public EQueryType getType() {
    return type;
  }

  public void setType(EQueryType type) {
    this.type = type;
  }

  public CoordinateBounds getBounds() {
    return bounds;
  }

  public void setBounds(CoordinateBounds bounds) {
    this.bounds = bounds;
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public int getMaxCount() {
    return maxCount;
  }

  public void setMaxCount(int maxCount) {
    this.maxCount = maxCount;
  }
}