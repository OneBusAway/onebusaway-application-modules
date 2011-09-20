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
package org.onebusaway.federations.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.onebusaway.federations.FederatedServiceRegistry;
import org.onebusaway.federations.FederatedServiceRegistryEntry;

public class FederatedServiceRegistryImplTest {

  @Test
  public void test() {
    FederatedServiceRegistryImpl registry = new FederatedServiceRegistryImpl();

    registry.addService("a1", TestA.class.getName(), map("type", "test"));
    registry.addService("a2", TestA.class.getName(), map("type", "prod"));
    registry.addService("b1", TestB.class.getName(), map("type", "test"));
    registry.addService("b2", TestB.class.getName(), map("type", "prod"));

    List<String> urls = getServiceUrls(registry, TestA.class, map());
    assertEquals(2, urls.size());
    assertTrue(urls.contains("a1"));
    assertTrue(urls.contains("a2"));

    urls = getServiceUrls(registry, TestA.class, map("type", "test"));
    assertEquals(1, urls.size());
    assertTrue(urls.contains("a1"));

    urls = getServiceUrls(registry, TestA.class, map("type", "prod"));
    assertEquals(1, urls.size());
    assertTrue(urls.contains("a2"));

    urls = getServiceUrls(registry, TestA.class, map("type", "what"));
    assertEquals(0, urls.size());

    urls = getServiceUrls(registry, TestB.class, map());
    assertEquals(2, urls.size());
    assertTrue(urls.contains("b1"));
    assertTrue(urls.contains("b2"));

    registry.setServiceStatus("a1", false);

    urls = getServiceUrls(registry, TestA.class, map());
    assertEquals(1, urls.size());
    assertTrue(urls.contains("a2"));

    urls = getServiceUrls(registry, TestA.class, map("type", "test"));
    assertEquals(0, urls.size());

    registry.setServiceStatus("a1", true);

    urls = getServiceUrls(registry, TestA.class, map());
    assertEquals(2, urls.size());
    assertTrue(urls.contains("a1"));
    assertTrue(urls.contains("a2"));

    urls = getServiceUrls(registry, TestA.class, map("type", "test"));
    assertEquals(1, urls.size());
    assertTrue(urls.contains("a1"));

    registry.removeService("b1");

    urls = getServiceUrls(registry, TestB.class, map());
    assertEquals(1, urls.size());
    assertTrue(urls.contains("b2"));

    urls = getServiceUrls(registry, TestB.class, map("type", "test"));
    assertEquals(0, urls.size());

    registry.removeAllServices();

    List<FederatedServiceRegistryEntry> services = registry.getAllServices();
    assertTrue(services.isEmpty());
  }

  @Test
  public void testReregistration() {

    FederatedServiceRegistryImpl registry = new FederatedServiceRegistryImpl();
    registry.addService("a1", TestA.class.getName(), map());
    registry.setServiceStatus("a1", false);

    List<FederatedServiceRegistryEntry> services = registry.getServices(
        TestA.class.getName(), map());
    assertEquals(0, services.size());

    registry.addService("a1", TestA.class.getName(), map());

    services = registry.getServices(TestA.class.getName(), map());
    assertEquals(0, services.size());

    registry.setServiceStatus("a1", true);

    services = registry.getServices(TestA.class.getName(), map());
    assertEquals(1, services.size());
  }

  private List<String> getServiceUrls(FederatedServiceRegistry registry,
      Class<?> serviceClass, Map<String, String> properties) {
    List<FederatedServiceRegistryEntry> entries = registry.getServices(
        serviceClass.getName(), properties);
    List<String> urls = new ArrayList<String>();
    for (FederatedServiceRegistryEntry entry : entries)
      urls.add(entry.getServiceUrl());
    Collections.sort(urls);
    return urls;
  }

  private Map<String, String> map(String... values) {

    if (values.length % 2 != 0)
      throw new IllegalArgumentException(
          "expected even number of key-value pairs");

    Map<String, String> m = new HashMap<String, String>();
    for (int i = 0; i < values.length; i += 2)
      m.put(values[i], values[i + 1]);

    return m;
  }

  private static interface TestA {

  }

  private static interface TestB {

  }
}
