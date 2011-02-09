package org.onebusaway.container.cache;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Test;

public class DefaultCacheableKeyFactoryTest {

  @Test
  public void test01() throws SecurityException, NoSuchMethodException {

    MockServiceImpl service = new MockServiceImpl();
    Method method = MockService.class.getMethod("evalaute", String.class);

    ProceedingJoinPoint pjp = ProceedingJoinPointFactory.create(service,
        service, MockService.class, method, "value");

    CacheableObjectKeyFactory[] keyFactories = {new DefaultCacheableObjectKeyFactory()};
    DefaultCacheableKeyFactory factory = new DefaultCacheableKeyFactory(
        keyFactories);

    CacheKeyInfo keyInfo = factory.createKey(pjp);
    assertEquals("value", keyInfo.getKey());
  }

  @Test
  public void test02() throws SecurityException, NoSuchMethodException {

    MockServiceImpl service = new MockServiceImpl();
    Method method = MockService.class.getMethod("evalauteMultiArg",
        String.class, String.class);

    ProceedingJoinPoint pjp = ProceedingJoinPointFactory.create(service,
        service, MockService.class, method, "valueA", "valueB");

    CacheableObjectKeyFactory[] keyFactories = {
        new DefaultCacheableObjectKeyFactory(),
        new DefaultCacheableObjectKeyFactory()};

    DefaultCacheableKeyFactory factory = new DefaultCacheableKeyFactory(
        keyFactories);

    DefaultCacheableKeyFactory.KeyImpl expected = new DefaultCacheableKeyFactory.KeyImpl(
        new Serializable[] {"valueA", "valueB"});

    CacheKeyInfo keyInfo = factory.createKey(pjp);
    assertEquals(expected, keyInfo.getKey());
  }

}
