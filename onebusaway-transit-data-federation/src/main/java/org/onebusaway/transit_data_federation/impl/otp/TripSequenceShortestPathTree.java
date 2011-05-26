/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.onebusaway.transit_data_federation.impl.otp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.collections.Max;
import org.onebusaway.transit_data_federation.impl.otp.graph.SearchLocal;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.Vertex;
import org.opentripplanner.routing.spt.AbstractShortestPathTree;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.routing.spt.SPTVertex;
import org.opentripplanner.routing.spt.ShortestPathTree;
import org.opentripplanner.routing.spt.ShortestPathTreeFactory;

public class TripSequenceShortestPathTree extends AbstractShortestPathTree {

  public static final ShortestPathTreeFactory FACTORY = new FactoryImpl();

  private Map<Vertex, Map<TripSequence, SPTVertex>> sptVerticesByTripSequence = new HashMap<Vertex, Map<TripSequence, SPTVertex>>();

  public Map<TripSequence, SPTVertex> getVerticesByTripSequence(Vertex vertex) {
    Map<TripSequence, SPTVertex> map = sptVerticesByTripSequence.get(vertex);
    if (map == null)
      return Collections.emptyMap();
    return map;
  }

  /****
   * {@link ShortestPathTree} Interface
   ****/

  @Override
  public SPTVertex addVertex(Vertex vertex, State state, double weightSum,
      TraverseOptions options) {

    OBATraverseOptions opts = (OBATraverseOptions) options;
    OBAStateData data = (OBAStateData) state.getData();
    TripSequence tripSequence = data.getTripSequence();

    Map<TripSequence, SPTVertex> map = getTripSequenceAndSptVerticesForVertex(vertex);

    SPTVertex existing = map.get(tripSequence);

    /**
     * We only keep the N-best itineraries. If adding this itinerary will push
     * us over the limit, figure out which one we should prune.
     */
    if (existing == null && map.size() == opts.numItineraries) {
      Max<TripSequence> m = new Max<TripSequence>();
      for (Map.Entry<TripSequence, SPTVertex> entry : map.entrySet()) {
        TripSequence key = entry.getKey();
        SPTVertex v = entry.getValue();
        m.add(v.weightSum, key);
      }

      /**
       * If the current max value is LESS than the new vertex, we don't add the
       * new vertex
       */
      double v = m.getMaxValue();
      if (v < weightSum)
        return null;

      /**
       * If the current max value is MORE than the new vertex, we kill the max
       * trip sequence in preparation for adding the new, better-scoring one
       */
      TripSequence key = m.getMaxElement();
      map.remove(key);
    }

    if (existing == null || weightSum < existing.weightSum) {
      SPTVertex ret = new SPTVertex(vertex, state, weightSum, options);
      map.put(tripSequence, ret);
      return ret;
    }

    return null;
  }

  @Override
  public GraphPath getPath(Vertex dest) {
    return getPath(dest, true);
  }

  @Override
  public GraphPath getPath(Vertex dest, boolean optimize) {
    SPTVertex end = null;
    Map<TripSequence, SPTVertex> map = sptVerticesByTripSequence.get(dest);
    if (map == null)
      return null;

    for (SPTVertex v : map.values()) {
      if (end == null || v.weightSum < end.weightSum) {
        end = v;
      }
    }

    return createPathForVertex(end, optimize);
  }

  @Override
  public List<GraphPath> getPaths(Vertex dest, boolean optimize) {
    Map<TripSequence, SPTVertex> map = sptVerticesByTripSequence.get(dest);
    if (map == null)
      return Collections.emptyList();
    List<GraphPath> paths = new ArrayList<GraphPath>();
    for (SPTVertex vertex : map.values()) {
      GraphPath path = createPathForVertex(vertex, optimize);
      paths.add(path);
    }
    return paths;
  }

  @Override
  public void removeVertex(SPTVertex vertex) {
    throw new UnsupportedOperationException();
  }

  public String toString() {
    return "SPT " + this.sptVerticesByTripSequence.toString();
  }

  /****
   * Private Methods
   ****/

  private Map<TripSequence, SPTVertex> getTripSequenceAndSptVerticesForVertex(
      Vertex vertex) {

    if (vertex instanceof SearchLocal) {
      SearchLocal local = (SearchLocal) vertex;
      Map<TripSequence, SPTVertex> map = local.getSearchLocalValue();
      if (map == null) {
        map = new HashMap<TripSequence, SPTVertex>();
        local.setSearchLocalValue(map);
      }
      return map;
    }

    Map<TripSequence, SPTVertex> map = sptVerticesByTripSequence.get(vertex);

    if (map == null) {
      map = new HashMap<TripSequence, SPTVertex>();
      sptVerticesByTripSequence.put(vertex, map);
    }
    return map;
  }

  private static final class FactoryImpl implements ShortestPathTreeFactory {

    @Override
    public ShortestPathTree create() {
      return new TripSequenceShortestPathTree();
    }
  }
}