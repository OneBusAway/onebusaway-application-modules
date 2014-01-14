/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
