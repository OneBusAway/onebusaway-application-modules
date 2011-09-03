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
import org.opentripplanner.routing.core.Vertex;
import org.opentripplanner.routing.spt.AbstractShortestPathTree;
import org.opentripplanner.routing.spt.ShortestPathTree;
import org.opentripplanner.routing.spt.ShortestPathTreeFactory;

public class TripSequenceShortestPathTree extends AbstractShortestPathTree {

  public static final ShortestPathTreeFactory FACTORY = new FactoryImpl();

  private Map<Vertex, ResultCollection> _sptVerticesByTripSequence = new HashMap<Vertex, ResultCollection>();

  public ResultCollection getVerticesByTripSequence(Vertex vertex) {
    return _sptVerticesByTripSequence.get(vertex);
  }

  /****
   * {@link ShortestPathTree} Interface
   ****/

  @Override
  public boolean add(State state) {

    OBATraverseOptions opts = (OBATraverseOptions) state.getOptions();
    OBAState obaState = (OBAState) state;
    TripSequence tripSequence = obaState.getTripSequence();

    ResultCollection collection = getCollectionForVertex(state.getVertex());

    Map<TripSequence, OBAState> map = collection.states;

    OBAState existing = map.get(tripSequence);

    /**
     * We only keep the N-best non-lookahead itineraries. If adding this
     * itinerary will push us over the limit, figure out which one we should
     * prune. If the itinerary to add is a lookahead, it won't affect the count,
     * so we don't have to check in that case.
     */
    if (existing == null && collection.itineraryCount == opts.numItineraries
        && !obaState.isLookaheadItinerary()) {

      Max<TripSequence> m = new Max<TripSequence>();

      for (Map.Entry<TripSequence, OBAState> entry : map.entrySet()) {
        TripSequence key = entry.getKey();
        OBAState v = entry.getValue();
        /**
         * Only check against non-lookahead itineraries
         */
        if (!v.isLookaheadItinerary())
          m.add(v.getWeight(), key);
      }

      /**
       * If the current max value is LESS than the new vertex, we don't add the
       * new vertex
       */
      double v = m.getMaxValue();
      if (v < state.getWeight())
        return false;

      /**
       * If the current max value is MORE than the new vertex, we kill the max
       * trip sequence in preparation for adding the new, better-scoring one
       */
      TripSequence key = m.getMaxElement();
      map.remove(key);
      collection.itineraryCount++;
    }

    if (existing == null || state.getWeight() < existing.getWeight()) {

      /**
       * If the existing itinerary is a non-lookahead, we need to adjust the
       * count.
       */
      if (existing != null) {
        if (!existing.isLookaheadItinerary())
          collection.itineraryCount--;
      }

      map.put(tripSequence, obaState);

      /**
       * If the new itinerary is a non-lookahead, we need to adjust the count.
       */
      if (!obaState.isLookaheadItinerary())
        collection.itineraryCount++;

      return true;
    }

    return false;
  }

  @Override
  public boolean visit(State s) {
    return true;
  }

  @Override
  public List<State> getStates(Vertex dest) {
    ResultCollection rc = _sptVerticesByTripSequence.get(dest);
    if (rc == null)
      return Collections.emptyList();
    return new ArrayList<State>(rc.states.values());
  }

  @Override
  public State getState(Vertex dest) {
    List<State> states = getStates(dest);
    State ret = null;
    for (State s : states) {
      if (ret == null || s.betterThan(ret)) {
        ret = s;
      }
    }
    return ret;
  }

  @Override
  public int getVertexCount() {
    return _sptVerticesByTripSequence.size();
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

    ResultCollection collection = _sptVerticesByTripSequence.get(vertex);

    if (collection == null) {
      collection = new ResultCollection();
      _sptVerticesByTripSequence.put(vertex, collection);
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

    private Map<TripSequence, OBAState> states = new HashMap<TripSequence, OBAState>();

    private int itineraryCount;

    public Map<TripSequence, OBAState> getStates() {
      return states;
    }

    public int getItineraryCount() {
      return itineraryCount;
    }
  }

}