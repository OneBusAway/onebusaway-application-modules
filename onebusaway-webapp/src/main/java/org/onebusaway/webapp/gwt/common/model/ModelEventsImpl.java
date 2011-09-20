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
package org.onebusaway.webapp.gwt.common.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelEventsImpl implements ModelEventsSourceAndSink {

  private Map<Class<?>, List<ModelListener<?>>> _listenersByType = new HashMap<Class<?>, List<ModelListener<?>>>();

  /*****************************************************************************
   * {@link ModelEventsSource} Interface
   ****************************************************************************/

  public <T> void addModelListener(Class<T> modelType, ModelListener<T> listener) {
    List<ModelListener<?>> listeners = _listenersByType.get(modelType);
    if (listeners == null) {
      listeners = new ArrayList<ModelListener<?>>();
      _listenersByType.put(modelType, listeners);
    }
    listeners.add(listener);
  }

  public <T> void removeModelListener(Class<T> modelType, ModelListener<T> listener) {
    List<ModelListener<?>> listeners = _listenersByType.get(modelType);
    if (listeners != null)
      listeners.remove(listener);
  }

  /*****************************************************************************
   * {@link ModelEventsSink} Interface
   ****************************************************************************/

  @SuppressWarnings("unchecked")
  public <T> void fireChange(T model) {
    fireChange((Class<T>) model.getClass(), model);
  }

  @SuppressWarnings("unchecked")
  public <T, T2 extends T> void fireChange(Class<T> modelType, T2 model) {
    List<ModelListener<?>> listeners = _listenersByType.get(modelType);
    if (listeners != null) {
      for (ModelListener<?> listener : listeners) {
        ModelListener<T> m = (ModelListener<T>) listener;
        m.handleUpdate(model);
      }
    }
  }

  public <T> ModelEventSourceAndSink<T> getEventSourceAndSink(Class<T> modelType) {
    return new ModelImpl<T>(modelType);
  }

  public <T> ModelEventSource<T> getEventSource(Class<T> modelType) {
    return getEventSourceAndSink(modelType);
  }

  public <T> ModelEventSink<T> getEventSink(Class<T> modelType) {
    return getEventSourceAndSink(modelType);
  }

  private class ModelImpl<T> implements ModelEventSourceAndSink<T> {

    private Class<T> _modelType;

    public ModelImpl(Class<T> modelType) {
      _modelType = modelType;
    }

    public void addModelListener(ModelListener<T> listener) {
      ModelEventsImpl.this.addModelListener(_modelType, listener);
    }

    public void removeModelListener(ModelListener<T> listener) {
      ModelEventsImpl.this.removeModelListener(_modelType, listener);
    }

    public void fireModelChange(T model) {
      ModelEventsImpl.this.fireChange(_modelType, model);
    }
  }
}
