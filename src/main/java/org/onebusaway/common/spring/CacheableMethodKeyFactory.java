package org.onebusaway.common.spring;

import org.aspectj.lang.ProceedingJoinPoint;

import java.io.Serializable;

public interface CacheableMethodKeyFactory {
  public Serializable createKey(ProceedingJoinPoint point);
}
