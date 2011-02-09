package org.onebusaway.container.cache;

import org.aspectj.lang.ProceedingJoinPoint;

public class MockCacheableMethodKeyFactory implements CacheableMethodKeyFactory {

  @Override
  public CacheKeyInfo createKey(ProceedingJoinPoint point) {
    return new CacheKeyInfo("blah", false);
  }
}
