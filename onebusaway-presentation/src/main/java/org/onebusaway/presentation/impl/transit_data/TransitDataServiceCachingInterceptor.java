package org.onebusaway.presentation.impl.transit_data;

import java.lang.reflect.Method;
import java.util.List;

import net.sf.ehcache.Cache;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.onebusaway.container.cache.AbstractCacheableMethodCallManager;
import org.onebusaway.container.cache.CacheableMethodKeyFactory;

@Aspect
public class TransitDataServiceCachingInterceptor extends
    AbstractCacheableMethodCallManager {

  @Around("execution(* org.onebusaway.transit_data.services.TransitDataService.getRoute(..))")
  public Object getRoute(ProceedingJoinPoint pjp) throws Throwable {
    return super.evaluate(pjp);
  }
  
  @Around("execution(* org.onebusaway.transit_data.services.TransitDataService.getStop(..))")
  public Object getStop(ProceedingJoinPoint pjp) throws Throwable {
    return super.evaluate(pjp);
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

  @Override
  protected CacheableMethodKeyFactory getKeyFactory(ProceedingJoinPoint pjp) {
    List<Method> methods = getMatchingMethodsForJoinPoint(pjp);
    if (methods.size() != 1)
      throw new IllegalStateException("expected just one matching method for "
          + pjp.getSignature());
    return getCacheableMethodKeyFactoryForMethod(methods.get(0));
  }
}
