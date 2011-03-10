package org.onebusaway.presentation.impl.transit_data;

import net.sf.ehcache.Cache;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.onebusaway.container.cache.CacheableMethodManager;

@Aspect
public class TransitDataServiceCachingInterceptor extends
    CacheableMethodManager {

  @Around("execution(* org.onebusaway.transit_data.services.TransitDataService.getRoute(..))")
  public Object getRoute(ProceedingJoinPoint pjp) throws Throwable {
    return evaluate(pjp);
  }

  @Around("execution(* org.onebusaway.transit_data.services.TransitDataService.getStop(..))")
  public Object getStop(ProceedingJoinPoint pjp) throws Throwable {
    return evaluate(pjp);
  }

  @Override
  protected Cache createCache(ProceedingJoinPoint pjp, String name) {
    // 1000 elements in memory
    // overflow to disk
    // not eternal
    // max lifetime is an hour
    // max idle-time is an hour
    return new Cache(name, 1000, true, false, 60 * 60, 60 * 60);
  }
}
