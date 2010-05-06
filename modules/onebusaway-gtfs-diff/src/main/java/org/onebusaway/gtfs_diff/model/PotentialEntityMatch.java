package org.onebusaway.gtfs_diff.model;

public class PotentialEntityMatch<T> extends EntityMatch<T> {

  private double score;

  public PotentialEntityMatch(T entityA, T entityB, double score) {
    super(entityA, entityB);
    this.score = score;
  }

  public double getScore() {
    return score;
  }
}
