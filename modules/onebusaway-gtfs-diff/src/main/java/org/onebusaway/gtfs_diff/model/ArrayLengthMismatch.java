package org.onebusaway.gtfs_diff.model;


public class ArrayLengthMismatch extends Mismatch {

  private int lengthA;
  private int lengthB;

  public ArrayLengthMismatch(int lengthA, int lengthB) {
    this.lengthA = lengthA;
    this.lengthB = lengthB;
  }

  public int getLengthA() {
    return lengthA;
  }

  public int getLengthB() {
    return lengthB;
  }
}
