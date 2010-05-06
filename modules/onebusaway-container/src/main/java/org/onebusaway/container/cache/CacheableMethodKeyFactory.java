package org.onebusaway.container.cache;

import org.aspectj.lang.ProceedingJoinPoint;

import java.io.Serializable;

public interface CacheableMethodKeyFactory {
  public Serializable createKey(ProceedingJoinPoint point);
}
