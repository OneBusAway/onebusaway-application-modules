package org.onebusaway.gtfs_diff.model;

public class EntityMismatch extends Mismatch {

  private Object entityA;

  private Object entityB;

  public EntityMismatch(Object entityA, Object entityB) {
    this.entityA = entityA;
    this.entityB = entityB;
  }

  public Object getEntityA() {
    return entityA;
  }

  public Object getEntityB() {
    return entityB;
  }
}
