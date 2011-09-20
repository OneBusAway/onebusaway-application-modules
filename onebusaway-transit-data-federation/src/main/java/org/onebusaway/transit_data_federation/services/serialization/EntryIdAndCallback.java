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
package org.onebusaway.transit_data_federation.services.serialization;

import org.onebusaway.transit_data_federation.impl.transit_graph.TransitGraphImpl;

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