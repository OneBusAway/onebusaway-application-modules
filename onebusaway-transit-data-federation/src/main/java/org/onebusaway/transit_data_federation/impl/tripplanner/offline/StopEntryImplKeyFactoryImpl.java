/**
 * 
 */
package org.onebusaway.transit_data_federation.impl.tripplanner.offline;

import org.onebusaway.container.cache.CacheKeyInfo;
import org.onebusaway.container.cache.CacheableObjectKeyFactory;

class StopEntryImplKeyFactoryImpl implements CacheableObjectKeyFactory {
  public CacheKeyInfo createKey(Object object) {
    throw new IllegalStateException("what?");
    //return ((StopEntryImpl) object).getId();
  }
}