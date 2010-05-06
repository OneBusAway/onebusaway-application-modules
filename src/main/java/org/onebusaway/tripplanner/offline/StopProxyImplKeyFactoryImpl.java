/**
 * 
 */
package org.onebusaway.tripplanner.offline;

import org.onebusaway.common.spring.CacheableObjectKeyFactory;

import java.io.Serializable;

class StopProxyImplKeyFactoryImpl implements CacheableObjectKeyFactory {
  public Serializable createKey(Object object) {
    return ((StopProxyImpl) object).getStopId();
  }
}