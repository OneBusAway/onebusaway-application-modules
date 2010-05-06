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
package org.onebusaway.transit_data_federation.impl.tripplanner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AStarSearch {

  @SuppressWarnings("unchecked")
  public static <T extends AStarNode> Map<T, T> search(
      AStarProblem<T> problem, T from, T to) throws NoPathToGoalException {
    
    searchWithResults(problem, from, to);
    
    Map<T, T> cameFrom = new HashMap<T, T>();
    T current = to;
    while (true) {
      T cf = (T) current.getCameFrom();
      if (cf == null)
        return cameFrom;
      cameFrom.put(current, cf);
      current = cf;
    }
  }

  public static <T extends AStarNode> AStarResults<T> searchWithResults(
      AStarProblem<T> problem, T from, T to) throws NoPathToGoalException {

    AStarResults<T> r = new AStarResults<T>();
    List<WithDistance<T>> neighbors = new ArrayList<WithDistance<T>>();

    from.setDistanceFromStart(0.0);
    from.setOpen();

    r.putTotalDistanceEstimate(from, 0.0);

    while (r.hasOpenNodes()) {
      
      T x = r.getMinEstimatedNode();
      
      if (x.equals(to))
        return r;
      
      if( x.isClosed() )
        continue;

      x.setClosed();

      double xDistanceFromStart = x.getDistanceFromStart();

      neighbors.clear();
      Collection<WithDistance<T>> neighborsResult = problem.getNeighbors(x,
          neighbors);

      for (WithDistance<T> wd : neighborsResult) {

        T y = wd.getValue();
        double xyDistance = wd.getDistance();

        if (y.isClosed())
          continue;

        double tentativeDistanceFromStart = xDistanceFromStart + xyDistance;
        boolean tentativeDistanceFromStartIsBetter = false;

        if (!y.isOpen()) {
          y.setOpen();
          double estimatedDistanceToEnd = problem.getEstimatedDistance(y, to);
          y.setEstimatedDistanceToEnd(estimatedDistanceToEnd);
          tentativeDistanceFromStartIsBetter = true;
        } else if (tentativeDistanceFromStart < y.getDistanceFromStart()) {
          tentativeDistanceFromStartIsBetter = true;
        }

        if (tentativeDistanceFromStartIsBetter) {

          double estimatedDistanceToEnd = y.getEstimatedDistanceToEnd();
          double totalEstimatedDistance = tentativeDistanceFromStart
              + estimatedDistanceToEnd;

          y.setCameFrom(x);
          y.setDistanceFromStart(tentativeDistanceFromStart);

          if (!problem.isValid(y, tentativeDistanceFromStart,
              estimatedDistanceToEnd)) {
            y.setClosed();
            continue;
          }

          r.putTotalDistanceEstimate(y, totalEstimatedDistance);
        }
      }
    }

    throw new NoPathToGoalException();
  }

  public static class NoPathToGoalException extends Exception {

    private static final long serialVersionUID = 1L;

  }
}
