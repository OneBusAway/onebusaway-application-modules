package org.onebusaway.container.model;

/**
 * Interface that defines that a specific object has listeners of type {@code T}
 * that can be added or removed.
 * 
 * @author bdferris
 * @see Listeners
 */
public interface HasListeners<T> {

  public void addListener(T listener);

  public void removeListener(T listener);
}
