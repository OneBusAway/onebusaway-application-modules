package org.onebusaway.container.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Convenience container for a collection of typed listener objects
 * 
 * @author bdferris
 * @see HasListeners
 */
public class Listeners<T> implements Iterable<T>, HasListeners<T> {

  private List<T> _listeners = new ArrayList<T>();

  public void addListener(T listener) {
    _listeners.add(listener);
  }

  public void removeListener(T listener) {
    _listeners.remove(listener);
  }

  @Override
  public Iterator<T> iterator() {
    return _listeners.iterator();
  }
}
