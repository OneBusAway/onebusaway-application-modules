package org.onebusaway.transit_data_federation.services.serialization;

/*****************************************************************************
 * Serialization Support
 ****************************************************************************/

public interface EntryCallback<T> {
  void handle(T entry);
}