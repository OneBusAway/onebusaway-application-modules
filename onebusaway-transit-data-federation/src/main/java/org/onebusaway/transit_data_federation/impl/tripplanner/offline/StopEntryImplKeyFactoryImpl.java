/**
 * 
 */
package org.onebusaway.transit_data_federation.impl.tripplanner.offline;

import org.onebusaway.container.cache.CacheableObjectKeyFactory;

import java.io.Serializable;

class StopEntryImplKeyFactoryImpl implements CacheableObjectKeyFactory {
  public Serializable createKey(Object object) {
    throw new IllegalStateException("what?");
    //return ((StopEntryImpl) object).getId();
  }
}