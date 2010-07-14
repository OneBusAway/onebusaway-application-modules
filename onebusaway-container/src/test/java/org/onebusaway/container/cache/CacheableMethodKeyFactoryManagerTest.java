package org.onebusaway.container.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import org.junit.Test;

public class CacheableMethodKeyFactoryManagerTest {

  @Test
  public void testServiceMethod01() throws SecurityException,
      NoSuchMethodException {

    Method method = MockServiceImpl.class.getMethod("evalaute", String.class);
    CacheableMethodKeyFactoryManager manager = new CacheableMethodKeyFactoryManager();

    CacheableMethodKeyFactory factory = manager.getCacheableMethodKeyFactoryForMethod(method);
    assertTrue(factory instanceof DefaultCacheableKeyFactory);

    DefaultCacheableKeyFactory defaultFactory = (DefaultCacheableKeyFactory) factory;
    assertEquals(1, defaultFactory.getNumberOfObjectKeyFactories());

    CacheableObjectKeyFactory objectKeyFactory = defaultFactory.getObjectKeyFactory(0);
    assertTrue(objectKeyFactory instanceof DefaultCacheableObjectKeyFactory);

    DefaultCacheableObjectKeyFactory defaultObjectKeyFactory = (DefaultCacheableObjectKeyFactory) objectKeyFactory;
    assertFalse(defaultObjectKeyFactory.isCacheRefreshCheck());
  }

  @Test
  public void testServiceMethod02() throws SecurityException,
      NoSuchMethodException {

    Method method = MockServiceImpl.class.getMethod("evalauteBean",
        MockBean.class);
    CacheableMethodKeyFactoryManager manager = new CacheableMethodKeyFactoryManager();

    CacheableMethodKeyFactory factory = manager.getCacheableMethodKeyFactoryForMethod(method);
    assertTrue(factory instanceof DefaultCacheableKeyFactory);

    DefaultCacheableKeyFactory defaultFactory = (DefaultCacheableKeyFactory) factory;
    assertEquals(1, defaultFactory.getNumberOfObjectKeyFactories());

    CacheableObjectKeyFactory objectKeyFactory = defaultFactory.getObjectKeyFactory(0);
    assertTrue(objectKeyFactory instanceof DefaultCacheableObjectKeyFactory);

    DefaultCacheableObjectKeyFactory defaultObjectKeyFactory = (DefaultCacheableObjectKeyFactory) objectKeyFactory;
    assertFalse(defaultObjectKeyFactory.isCacheRefreshCheck());
  }

  @Test
  public void testServiceMethod03() throws SecurityException,
      NoSuchMethodException {

    Method method = MockServiceImpl.class.getMethod(
        "evalauteBeanWithAnnotation", MockBean.class);
    CacheableMethodKeyFactoryManager manager = new CacheableMethodKeyFactoryManager();

    CacheableMethodKeyFactory factory = manager.getCacheableMethodKeyFactoryForMethod(method);
    assertTrue(factory instanceof MockCacheableMethodKeyFactory);
  }

  @Test
  public void testServiceMethod04() throws SecurityException,
      NoSuchMethodException {

    Method method = MockServiceImpl.class.getMethod(
        "evalauteBeanWithParameterAnnotation", MockBean.class, Boolean.TYPE);
    CacheableMethodKeyFactoryManager manager = new CacheableMethodKeyFactoryManager();

    CacheableMethodKeyFactory factory = manager.getCacheableMethodKeyFactoryForMethod(method);
    assertTrue(factory instanceof DefaultCacheableKeyFactory);

    DefaultCacheableKeyFactory defaultFactory = (DefaultCacheableKeyFactory) factory;
    assertEquals(2, defaultFactory.getNumberOfObjectKeyFactories());

    CacheableObjectKeyFactory objectKeyFactoryA = defaultFactory.getObjectKeyFactory(0);
    assertTrue(objectKeyFactoryA instanceof PropertyPathExpressionCacheableObjectKeyFactory);

    CacheableObjectKeyFactory objectKeyFactoryB = defaultFactory.getObjectKeyFactory(1);
    assertTrue(objectKeyFactoryB instanceof DefaultCacheableObjectKeyFactory);

    DefaultCacheableObjectKeyFactory defaultObjectKeyFactoryB = (DefaultCacheableObjectKeyFactory) objectKeyFactoryB;
    assertTrue(defaultObjectKeyFactoryB.isCacheRefreshCheck());
  }

  @Test
  public void testServiceMethod05() throws SecurityException,
      NoSuchMethodException {

    Method method = MockServiceImpl.class.getMethod(
        "evalauteBeanWithoutParameterAnnotation", MockBean.class, Boolean.TYPE);
    CacheableMethodKeyFactoryManager manager = new CacheableMethodKeyFactoryManager();
    manager.putCacheRefreshIndicatorArgumentIndexForMethodSignature(
        MockServiceImpl.class.getName()
            + ".evalauteBeanWithoutParameterAnnotation", 1);

    CacheableMethodKeyFactory factory = manager.getCacheableMethodKeyFactoryForMethod(method);
    assertTrue(factory instanceof DefaultCacheableKeyFactory);

    DefaultCacheableKeyFactory defaultFactory = (DefaultCacheableKeyFactory) factory;
    assertEquals(2, defaultFactory.getNumberOfObjectKeyFactories());

    CacheableObjectKeyFactory objectKeyFactoryA = defaultFactory.getObjectKeyFactory(0);
    assertTrue(objectKeyFactoryA instanceof DefaultCacheableObjectKeyFactory);

    CacheableObjectKeyFactory objectKeyFactoryB = defaultFactory.getObjectKeyFactory(1);
    assertTrue(objectKeyFactoryB instanceof DefaultCacheableObjectKeyFactory);

    DefaultCacheableObjectKeyFactory defaultObjectKeyFactoryB = (DefaultCacheableObjectKeyFactory) objectKeyFactoryB;
    assertTrue(defaultObjectKeyFactoryB.isCacheRefreshCheck());
  }
}
