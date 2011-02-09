package org.onebusaway.container.cache;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;

import net.sf.ehcache.CacheManager;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Test;

public class CacheableMethodManagerTest {
  @Test
  public void test() throws Throwable {

    CacheableMethodKeyFactoryManager factoryManager = new CacheableMethodKeyFactoryManager();
    CacheManager cacheManager = new CacheManager(getClass().getResource(
        "ehcache-test.xml"));

    CacheableMethodManager manager = new CacheableMethodManager();
    manager.setCacheableMethodKeyFactoryManager(factoryManager);
    manager.setCacheManager(cacheManager);

    MockServiceImpl impl = new MockServiceImpl();
    Method method = MockServiceImpl.class.getMethod(
        "evalauteBeanWithParameterAnnotation", MockBean.class, Boolean.TYPE);

    MockBean bean = new MockBean();
    bean.setId("id");

    ProceedingJoinPoint pjp = ProceedingJoinPointFactory.create(impl, impl,
        MockService.class, method, bean, false);
    Object value = manager.evaluate(pjp);

    assertEquals("test", value);
    assertEquals(1, impl.getEvalauteBeanWithParameterAnnotationCount());

    value = manager.evaluate(pjp);

    assertEquals("test", value);
    assertEquals(1, impl.getEvalauteBeanWithParameterAnnotationCount());

    /**
     * This time we indicate we want a cache refresh
     */
    ProceedingJoinPoint pjp2 = ProceedingJoinPointFactory.create(impl, impl,
        MockService.class, method, bean, true);
    value = manager.evaluate(pjp2);

    assertEquals("test", value);
    assertEquals(2, impl.getEvalauteBeanWithParameterAnnotationCount());
  }
}
