/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.presentation.impl.transit_data;

import javax.annotation.PostConstruct;

import net.sf.ehcache.Cache;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.onebusaway.container.cache.CacheableMethodManager;
import org.onebusaway.transit_data.model.ArrivalsAndDeparturesQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;

/**
 * To reduce the load on the {@link TransitDataService}, we can
 * opportunistically cache a lot of stuff on the client side.
 * 
 * @author bdferris
 */
@Aspect
public class TransitDataServiceCachingInterceptor extends
    CacheableMethodManager {

  /**
   * Time, in seconds, that arrival and departure records should be cached
   */
  private int _arrivalAndDepartureCacheWindow = 30;

  /**
   * Number of arrival and departure records that should be kept in cache memory
   */
  private int _arrivalAndDepartureCacheSize = 5000;

  @PostConstruct
  public void setup() {
    _cacheableMethodKeyFactoryManager.addCacheableObjectKeyFactory(
        ArrivalsAndDeparturesQueryBean.class,
        new ArrivalsAndDeparturesQueryBeanCacheableObjectKeyFactory(
            _arrivalAndDepartureCacheWindow));
  }

  /**
   * 
   * @param arrivalAndDepartureCacheWindow in seconds
   */
  public void setArrivalAndDepartureCacheWindow(
      int arrivalAndDepartureCacheWindow) {
    _arrivalAndDepartureCacheWindow = arrivalAndDepartureCacheWindow;
  }

  public void setArrivalAndDepartureCacheSize(int arrivalAndDepartureCacheSize) {
    _arrivalAndDepartureCacheSize = arrivalAndDepartureCacheSize;
  }

  @Around("execution(* org.onebusaway.transit_data.services.TransitDataService.getRoute(..))")
  public Object getRoute(ProceedingJoinPoint pjp) throws Throwable {
    return evaluate(pjp);
  }

  @Around("execution(* org.onebusaway.transit_data.services.TransitDataService.getStop(..))")
  public Object getStop(ProceedingJoinPoint pjp) throws Throwable {
    return evaluate(pjp);
  }

  @Around("execution(* org.onebusaway.transit_data.services.TransitDataService.getStopsForRoute(..))")
  public Object getStopsForRoute(ProceedingJoinPoint pjp) throws Throwable {
    return evaluate(pjp);
  }

  @Around("execution(* org.onebusaway.transit_data.services.TransitDataService.getSingleTripDetails(..))")
  public Object getSingleTripDetails(ProceedingJoinPoint pjp) throws Throwable {
    return evaluate(pjp);
  }

  @Around("execution(* org.onebusaway.transit_data.services.TransitDataService.getStopWithArrivalsAndDepartures(..))")
  public Object getStopWithArrivalsAndDepartures(ProceedingJoinPoint pjp)
      throws Throwable {
    return evaluate(pjp);
  }

  @Override
  protected Cache createCache(ProceedingJoinPoint pjp, String name) {

    if (name.equals("org.onebusaway.transit_data.services.TransitDataService.getStopWithArrivalsAndDepartures")) {
      // _arrivalAndDepartureCacheSize elements in memory
      // do overflow to disk
      // not eternal
      // max lifetime is the arrival and departure cache window
      // max idle-time is the arrival and departure cache window
      return new Cache(name, _arrivalAndDepartureCacheSize, true, false,
          _arrivalAndDepartureCacheWindow, _arrivalAndDepartureCacheWindow);
    }

    // 1000 elements in memory
    // overflow to disk
    // not eternal
    // max lifetime is an hour
    // max idle-time is an hour
    return new Cache(name, 1000, true, false, 60 * 60, 60 * 60);
  }
}
