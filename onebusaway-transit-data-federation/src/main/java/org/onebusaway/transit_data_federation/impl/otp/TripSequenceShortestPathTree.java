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

  private Map<Vertex, ResultCollection> sptVerticesByTripSequence = new HashMap<Vertex, ResultCollection>();

  public ResultCollection getVerticesByTripSequence(Vertex vertex) {
    return sptVerticesByTripSequence.get(vertex);
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

    ResultCollection collection = getCollectionForVertex(vertex);

    Map<TripSequence, SPTVertex> map = collection.vertices;

    SPTVertex existing = map.get(tripSequence);
    
    /**
     * We only keep the N-best non-lookahead itineraries. If adding this
     * itinerary will push us over the limit, figure out which one we should
     * prune. If the itinerary to add is a lookahead, it won't affect the count,
     * so we don't have to check in that case.
     */
    if (existing == null && collection.itineraryCount == opts.numItineraries
        && !data.isLookaheadItinerary()) {

      Max<TripSequence> m = new Max<TripSequence>();

      for (Map.Entry<TripSequence, SPTVertex> entry : map.entrySet()) {
        TripSequence key = entry.getKey();
        SPTVertex v = entry.getValue();
        OBAStateData vData = (OBAStateData) v.state.getData();
        /**
         * Only check against non-lookahead itineraries
         */
        if (!vData.isLookaheadItinerary())
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
      collection.itineraryCount++;
    }

    if (existing == null || weightSum < existing.weightSum) {

      /**
       * If the existing itinerary is a non-lookahead, we need to adjust the
       * count.
       */
      if (existing != null) {
        OBAStateData vData = (OBAStateData) existing.state.getData();
        if (!vData.isLookaheadItinerary())
          collection.itineraryCount--;
      }

      SPTVertex ret = new SPTVertex(vertex, state, weightSum, options);
      map.put(tripSequence, ret);

      /**
       * If the new itinerary is a non-lookahead, we need to adjust the count.
       */
      if (!data.isLookaheadItinerary())
        collection.itineraryCount++;

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
    ResultCollection collection = sptVerticesByTripSequence.get(dest);
    if (collection == null)
      return null;

    for (SPTVertex v : collection.vertices.values()) {
      if (end == null || v.weightSum < end.weightSum) {
        end = v;
      }
    }

    return createPathForVertex(end, optimize);
  }

  @Override
  public List<GraphPath> getPaths(Vertex dest, boolean optimize) {
    ResultCollection collection = sptVerticesByTripSequence.get(dest);
    if (collection == null)
      return Collections.emptyList();
    List<GraphPath> paths = new ArrayList<GraphPath>();
    for (SPTVertex vertex : collection.vertices.values()) {
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

  private ResultCollection getCollectionForVertex(Vertex vertex) {

    if (vertex instanceof SearchLocal) {
      SearchLocal local = (SearchLocal) vertex;
      ResultCollection collection = local.getSearchLocalValue();
      if (collection == null) {
        collection = new ResultCollection();
        local.setSearchLocalValue(collection);
      }
      return collection;
    }

    ResultCollection collection = sptVerticesByTripSequence.get(vertex);

    if (collection == null) {
      collection = new ResultCollection();
      sptVerticesByTripSequence.put(vertex, collection);
    }
    return collection;
  }

  private static final class FactoryImpl implements ShortestPathTreeFactory {

    @Override
    public ShortestPathTree create() {
      return new TripSequenceShortestPathTree();
    }
  }

  public static class ResultCollection {

    private Map<TripSequence, SPTVertex> vertices = new HashMap<TripSequence, SPTVertex>();

    private int itineraryCount;
    
    public Map<TripSequence, SPTVertex> getVertices() {
      return vertices;
    }
    
    public int getItineraryCount() {
      return itineraryCount;
    }
  }
}