package org.onebusaway.gtfs_diff.model;


public class ArrayIndexMatch extends Match {

  private int index;

  public ArrayIndexMatch(int index) {
    this.index = index;
  }

  public int getIndex() {
    return index;
  }
}
