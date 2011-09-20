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
package org.onebusaway.container.spring;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.onebusaway.container.model.HasListeners;

/**
 * Spring convenience class for wiring up {@link HasListeners} listener
 * containers with listener objects. Specify an arbitrary number of source
 * containers and listeners and they will be wired up in a {@link PostConstruct}
 * phase.
 * 
 * @author bdferris
 */
public class ListenerConnector<T> {

  private HasListeners<T> _source;

  private T _listener;

  private List<HasListeners<T>> _sources;

  public void setSource(HasListeners<T> source) {
    _source = source;
  }

  public void setSources(List<HasListeners<T>> sources) {
    _sources = sources;
  }

  public void setListener(T listener) {
    _listener = listener;
  }

  @PostConstruct
  public void start() {
    addListener(_source, _listener);
    if (_sources != null) {
      for (HasListeners<T> source : _sources)
        addListener(source, _listener);
    }
  }

  @PreDestroy
  public void stop() {
    removeListener(_source, _listener);
    if (_sources != null) {
      for (HasListeners<T> source : _sources)
        removeListener(source, _listener);
    }
  }

  private void addListener(HasListeners<T> source, T listener) {
    if (source != null && listener != null)
      source.addListener(listener);
  }

  private void removeListener(HasListeners<T> source, T listener) {
    if (source != null && listener != null)
      source.removeListener(listener);
  }
}
