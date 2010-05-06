/**
 * 
 */
package org.onebusaway.transit_data_federation.services.serialization;

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