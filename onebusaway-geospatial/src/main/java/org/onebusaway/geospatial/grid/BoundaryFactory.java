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
package org.onebusaway.geospatial.grid;

import org.onebusaway.utility.collections.TreeUnionFind;
import org.onebusaway.utility.collections.TreeUnionFind.Sentry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BoundaryFactory {

  private boolean _pruneAllButCorners = false;

  public void setPruneAllButCorners(boolean pruneAllButCorners) {
    _pruneAllButCorners = pruneAllButCorners;
  }

  public <T> List<Boundary> getBoundaries(Grid<T> grid) {
      return getBoundaries(grid,grid.getEntries());
  }
  
  public <T> List<Boundary> getBoundaries(Grid<T> grid, Iterable<Grid.Entry<T>> entries) {

    List<BoundaryContext> contexts = new ArrayList<BoundaryContext>();
    Set<BoundaryEdge> closed = new HashSet<BoundaryEdge>();

    TreeUnionFind<GridIndex> clusters = new TreeUnionFind<GridIndex>();

    for (Grid.Entry<?> entry : entries) {

      GridIndex index = entry.getIndex();

      for (EDirection direction : EDirection.values()) {

        BoundaryEdge edge = new BoundaryEdge(index, direction);

        if (!closed.add(edge))
          continue;

        if (isOpen(grid, index, direction)) {
          BoundaryContext context = new BoundaryContext(grid);
          exploreBoundary(context, index, direction, true);
          BoundaryPath path = context.getPath();
          for (int i = 0; i < path.size(); i++)
            closed.add(new BoundaryEdge(path.getIndex(i), path.getDirection(i)));
          contexts.add(context);
        } else {
          GridIndex adjacent = getIndex(index, direction);
          clusters.union(index, adjacent);
        }
      }
    }

    Map<Sentry, Boundary> boundariesByCluster = new HashMap<Sentry, Boundary>();
    List<Boundary> boundaries = new ArrayList<Boundary>();

    for (BoundaryContext context : contexts) {

      BoundaryPath path = context.getPath();
      GridIndex first = path.getIndex(0);
      Sentry sentry = clusters.find(first);
      Boundary boundary = boundariesByCluster.get(sentry);

      if (boundary == null) {
        boundary = new Boundary();
        boundariesByCluster.put(sentry, boundary);
        boundaries.add(boundary);
      }

      boolean outer = context.getTurns() >= 4;

      if (_pruneAllButCorners)
        path = pruneAllButCorners(path);

      if (outer) {
        if (boundary.getOuterBoundary() != null)
          throw new IllegalStateException("multiple outer boundaries");
        boundary.setOuterBoundary(path);
      } else {
        boundary.addInnerBoundary(path);
      }
    }

    return boundaries;
  }

  private boolean isOpen(Grid<?> grid, GridIndex index, EDirection direction) {
    return !grid.contains(getIndex(index, direction));
  }

  private GridIndex getIndex(GridIndex index, EDirection direction) {
    int x = index.getX();
    int y = index.getY();
    switch (direction) {
      case UP:
        y++;
        break;
      case RIGHT:
        x++;
        break;
      case DOWN:
        y--;
        break;
      case LEFT:
        x--;
        break;
      default:
        throw new IllegalStateException();
    }
    return new GridIndex(x, y);
  }

  private void exploreBoundary(BoundaryContext context, GridIndex index,
      EDirection direction, boolean isOpen) {

    if (context.isBackToStart(index, direction))
      return;

    if (isOpen) {

      context.addEdge(index, direction);

      EDirection nextDirection = direction.getNext();
      context.rightTurn();
      boolean open = isOpen(context.getGrid(), index, nextDirection);
      exploreBoundary(context, index, nextDirection, open);
    } else {
      GridIndex nextIndex = getIndex(index, direction);
      EDirection nextDirection = direction.getPrev();
      context.leftTurn();
      boolean open = isOpen(context.getGrid(), nextIndex, nextDirection);
      exploreBoundary(context, nextIndex, nextDirection, open);
    }
  }

  private BoundaryPath pruneAllButCorners(BoundaryPath path) {

    for (int offset = 0; offset < path.size(); offset++) {
      
      int pre = offset - 1;
      if (pre < 0)
        pre += path.size();
      
      if (!path.getDirection(pre).equals(path.getDirection(offset))) {
        EDirection prev = null;
        BoundaryPath reduced = new BoundaryPath();
        for (int i = 0; i < path.size(); i++) {
          int index = (i + offset) % path.size();
          GridIndex gridIndex = path.getIndex(index);
          EDirection direction = path.getDirection(index);
          if (prev == null || !prev.equals(direction))
            reduced.addEdge(gridIndex, direction);
          prev = direction;
        }
        return reduced;
      
      }
    }

    throw new IllegalStateException();
  }

  /*****************************************************************************
   * 
   ****************************************************************************/

  private static class BoundaryContext {

    private BoundaryPath _path = new BoundaryPath();

    private int _turns = 0;

    private Grid<?> _grid;

    public BoundaryContext(Grid<?> grid) {
      _grid = grid;
    }

    public boolean isEmpty() {
      return _path.isEmpty();
    }

    public EDirection getLastDirection() {
      return _path.getDirection(_path.size() - 1);
    }

    public Grid<?> getGrid() {
      return _grid;
    }

    public BoundaryPath getPath() {
      return _path;
    }

    public void addEdge(GridIndex index, EDirection direction) {
      _path.addEdge(index, direction);
    }

    public boolean isBackToStart(GridIndex index, EDirection direction) {
      if (_path.isEmpty())
        return false;

      GridIndex firstIndex = _path.getIndex(0);
      EDirection firstDirection = _path.getDirection(0);

      return index.equals(firstIndex) && direction.equals(firstDirection);
    }

    public int getTurns() {
      return _turns;
    }

    public void leftTurn() {
      _turns--;
    }

    public void rightTurn() {
      _turns++;
    }

  }

  private static class BoundaryEdge {

    private final GridIndex _index;

    private final EDirection _direction;

    public BoundaryEdge(GridIndex index, EDirection direction) {
      _index = index;
      _direction = direction;
    }

    public GridIndex getIndex() {
      return _index;
    }

    public EDirection getDirection() {
      return _direction;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result
          + ((_direction == null) ? 0 : _direction.hashCode());
      result = prime * result + ((_index == null) ? 0 : _index.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (!(obj instanceof BoundaryEdge))
        return false;
      BoundaryEdge other = (BoundaryEdge) obj;
      if (_direction == null) {
        if (other._direction != null)
          return false;
      } else if (!_direction.equals(other._direction))
        return false;
      if (_index == null) {
        if (other._index != null)
          return false;
      } else if (!_index.equals(other._index))
        return false;
      return true;
    }
  }

}
