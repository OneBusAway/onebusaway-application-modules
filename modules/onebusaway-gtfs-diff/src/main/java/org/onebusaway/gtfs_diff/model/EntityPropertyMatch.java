package org.onebusaway.gtfs_diff.model;

public class EntityPropertyMatch extends Match {

  private Object entityA;
  private Object entityB;
  private String propertyName;

  public EntityPropertyMatch(Object entityA, Object entityB,
      String propertyName) {
    this.entityA = entityA;
    this.entityB = entityB;
    this.propertyName = propertyName;
  }

  public Object getEntityA() {
    return entityA;
  }

  public Object getEntityB() {
    return entityB;
  }

  public String getPropertyName() {
    return propertyName;
  }
}
