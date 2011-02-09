/**
 * 
 */
package org.onebusaway.geospatial.grid;

public enum EDirection {

  UP, RIGHT, DOWN, LEFT;

  public EDirection getPrev() {
    switch (this) {
      case UP:
        return LEFT;
      case RIGHT:
        return UP;
      case DOWN:
        return RIGHT;
      case LEFT:
        return DOWN;
      default:
        throw new IllegalStateException();
    }
  }

  public EDirection getNext() {
    switch (this) {
      case UP:
        return RIGHT;
      case RIGHT:
        return DOWN;
      case DOWN:
        return LEFT;
      case LEFT:
        return UP;
      default:
        throw new IllegalStateException();
    }
  }
}