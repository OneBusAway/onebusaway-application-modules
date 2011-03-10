package org.onebusaway.container.cache;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Test;

public class DefaultCacheableKeyFactoryTest {

  @Test
  public void test() throws SecurityException, NoSuchMethodException {

    MockServiceImpl service = new MockServiceImpl();
    Method method = MockService.class.getMethod("evalaute", String.class);

    ProceedingJoinPoint pjp = ProceedingJoinPointFactory.create(service,
        service, MockService.class, method, "value");

    CacheableObjectKeyFactory[] keyFactories = {new DefaultCacheableObjectKeyFactory()};
    DefaultCacheableKeyFactory factory = new DefaultCacheableKeyFactory(
        keyFactories);

    DefaultCacheableKeyFactory.KeyImpl expected = new DefaultCacheableKeyFactory.KeyImpl(
        new Serializable[] {"value"});

    CacheKeyInfo keyInfo = factory.createKey(pjp);
    assertEquals(expected, keyInfo.getKey());
  }

}
