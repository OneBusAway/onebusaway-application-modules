package org.onebusaway.common.spring;

import org.aspectj.lang.ProceedingJoinPoint;
import org.onebusaway.common.model.IdentityBean;

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
