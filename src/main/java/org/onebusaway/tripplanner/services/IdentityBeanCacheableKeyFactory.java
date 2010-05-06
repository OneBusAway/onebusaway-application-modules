package org.onebusaway.tripplanner.services;

import org.aspectj.lang.ProceedingJoinPoint;
import org.onebusaway.common.model.IdentityBean;
import org.onebusaway.common.spring.CacheableMethodKeyFactory;

import java.io.Serializable;

public class IdentityBeanCacheableKeyFactory implements CacheableMethodKeyFactory {

  public Serializable createKey(ProceedingJoinPoint point) {
    StringBuilder b = new StringBuilder();
    for (Object arg : point.getArgs()) {
      if (b.length() > 0)
        b.append(',');
      IdentityBean<?> bean = (IdentityBean<?>) arg;
      b.append(bean.getId());
    }
    return b.toString();
  }
}
