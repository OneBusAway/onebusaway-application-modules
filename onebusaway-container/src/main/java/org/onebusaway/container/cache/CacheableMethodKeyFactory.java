package org.onebusaway.container.cache;

import org.aspectj.lang.ProceedingJoinPoint;

import java.io.Serializable;

/**
 * Factory for producing a {@link Serializable} cache key from a
 * {@link ProceedingJoinPoint} method invocation. Typically, the arguments to
 * the method are used to generate the cache key, though the details are left up
 * to the implementor.
 * 
 * @author bdferris
 * @see Cacheable#keyFactory()
 * @see DefaultCacheableKeyFactory
 */
public interface CacheableMethodKeyFactory {
  public CacheKeyInfo createKey(ProceedingJoinPoint point);
}
