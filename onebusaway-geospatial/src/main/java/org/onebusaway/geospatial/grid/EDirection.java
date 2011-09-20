/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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