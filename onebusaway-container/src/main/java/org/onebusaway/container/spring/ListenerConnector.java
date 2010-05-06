package org.onebusaway.container.spring;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.onebusaway.container.model.HasListeners;

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
