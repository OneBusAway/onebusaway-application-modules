package org.onebusaway.transit_data_federation.services.serialization;

/**
 * A simple callback interface with a single typed argument. Used in the
 * {@link EntryIdAndCallback} to support large object graph serialization.
 * 
 * @author bdferris
 * 
 * @param <T> callback
 * @see EntryIdAndCallback
 */
public interface EntryCallback<T> {
  void handle(T entry);
}