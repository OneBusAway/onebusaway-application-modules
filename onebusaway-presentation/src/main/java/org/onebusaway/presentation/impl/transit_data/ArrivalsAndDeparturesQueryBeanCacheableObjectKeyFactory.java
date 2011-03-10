package org.onebusaway.presentation.impl.transit_data;

import org.onebusaway.container.cache.CacheKeyInfo;
import org.onebusaway.container.cache.CacheableObjectKeyFactory;
import org.onebusaway.transit_data.model.ArrivalsAndDeparturesQueryBean;

public class ArrivalsAndDeparturesQueryBeanCacheableObjectKeyFactory implements
    CacheableObjectKeyFactory {

  private long _timeWindow = 30 * 1000;

  public ArrivalsAndDeparturesQueryBeanCacheableObjectKeyFactory(
      int arrivalAndDepartureCacheWindow) {
    _timeWindow = arrivalAndDepartureCacheWindow * 1000;
  }

  public void setTimeWindow(int timeWindowInSeconds) {
    _timeWindow = timeWindowInSeconds * 1000;
  }

  @Override
  public CacheKeyInfo createKey(Object object) {
    ArrivalsAndDeparturesQueryBean query = (ArrivalsAndDeparturesQueryBean) object;
    query = new ArrivalsAndDeparturesQueryBean(query);
    query.setTime(snapTime(query.getTime()));
    return new CacheKeyInfo(query, false);
  }

  private long snapTime(long time) {
    return (time / _timeWindow) * _timeWindow;
  }
}
