package org.onebusaway.gtfs_diff.model;

public class EntityMatch<T> extends Match {

  private T entityA;
  
  private T entityB;

  public EntityMatch(T entityA, T entityB) {
    this.entityA = entityA;
    this.entityB = entityB;
  }

  public T getEntityA() {
    return entityA;
  }

  public T getEntityB() {
    return entityB;
  }
}
