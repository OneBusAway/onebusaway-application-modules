/**
 * 
 */
package org.onebusaway.transit_data_federation.services.serialization;

import org.onebusaway.transit_data_federation.impl.transit_graph.TransitGraphImpl;
import org.onebusaway.transit_data_federation.impl.walkplanner.offline.WalkPlannerGraphImpl;

/**
 * A sort of delayed-binding mechanism to help with serializing large, highly
 * connected graphs of Java objects where you usually get a stack-depth
 * exception when the default DFS Java serialization mechanism does its thing.
 * This support class is to help where you replace your object references with
 * id references in a custom object serialization routine. Then you also have a
 * callback that repopulates the actual object reference upon deserialization.
 * 
 * @author bdferris
 * 
 * @param <K> the id type
 * @param <T> the callback result type
 * 
 * @see TransitGraphImpl
 * @see WalkPlannerGraphImpl
 */
public class EntryIdAndCallback<K, T> {
  private K _id;

  private EntryCallback<T> _callback;

  public EntryIdAndCallback(K id, EntryCallback<T> callback) {
    _callback = callback;
    _id = id;
  }

  public K getId() {
    return _id;
  }

  public EntryCallback<T> getCallback() {
    return _callback;
  }

}