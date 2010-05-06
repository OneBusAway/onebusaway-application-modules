/*
 * Copyright 2008 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.tripplanner.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AStarSearch {

  public static <T> Map<T, T> search(AStarProblem<T> problem, T from, T to) throws NoPathToGoalException {
    AStarResults<T> results = searchWithResults(problem, from, to);
    return results.getCameFrom();
  }

  public static <T> AStarResults<T> searchWithResults(AStarProblem<T> problem, T from, T to)
      throws NoPathToGoalException {

    AStarResults<T> r = new AStarResults<T>();
    List<T> neighbors = new ArrayList<T>();

    r.setDistanceFromStart(from, 0.0);
    r.putTotalDistanceEstimate(from, 0.0);
    r.setOpen(from);

    while (r.hasOpenNodes()) {

      T x = r.getMinEstimatedNode();

      if (x.equals(to))
        return r;

      r.setClosed(x);

      double xDistanceFromStart = r.getDistanceFromStart(x);

      neighbors.clear();
      Collection<T> neighborsResult = problem.getNeighbors(x, neighbors);

      for (T y : neighborsResult) {

        if (r.isClosed(y))
          continue;

        double xyDistance = problem.getDistance(x, y);
        double tentativeDistanceFromStart = xDistanceFromStart + xyDistance;
        boolean tentativeDistanceFromStartIsBetter = false;

        if (!r.isOpen(y)) {
          r.setOpen(y);
          double estimatedDistanceToEnd = problem.getEstimatedDistance(y, to);
          r.setEstimatedDistanceToEnd(y, estimatedDistanceToEnd);
          tentativeDistanceFromStartIsBetter = true;
        } else if (tentativeDistanceFromStart < r.getDistanceFromStart(y)) {
          tentativeDistanceFromStartIsBetter = true;
        }

        if (tentativeDistanceFromStartIsBetter) {
          r.getCameFrom().put(y, x);
          r.setDistanceFromStart(y, tentativeDistanceFromStart);
          double estimatedDistanceToEnd = r.getEstimatedDistanceToEnd(y);
          r.putTotalDistanceEstimate(y, tentativeDistanceFromStart + estimatedDistanceToEnd);
        }
      }
    }

    throw new NoPathToGoalException();
  }

  public static class NoPathToGoalException extends Exception {

    private static final long serialVersionUID = 1L;

  }
}
