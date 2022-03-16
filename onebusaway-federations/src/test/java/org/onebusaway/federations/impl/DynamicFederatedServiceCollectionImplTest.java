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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.onebusaway.federations.FederatedService;
import org.onebusaway.federations.SimpleFederatedService;
import org.onebusaway.geospatial.model.CoordinateBounds;

import com.caucho.hessian.server.HessianServlet;

public class DynamicFederatedServiceCollectionImplTest {

  private static final int TIMEOUT = 10000;

  private static final int PORT = 9991;

  private FederatedServiceRegistryImpl _registry;

  private DynamicFederatedServiceCollectionImpl _collection;

  private Server _server;

  @Before
  public void start() throws Exception {

    _server = new Server(PORT);

    addServiceServlet(_server, "A", new CoordinateBounds(0, 0, 10, 10));
    addServiceServlet(_server, "B", new CoordinateBounds(20, 20, 30, 30));

    Map<String, List<CoordinateBounds>> agenciesB = new HashMap<String, List<CoordinateBounds>>();
    agenciesB.put("B", Arrays.asList(new CoordinateBounds(30, 30, 40, 40)));

    _server.start();

    _registry = new FederatedServiceRegistryImpl();

    _collection = new DynamicFederatedServiceCollectionImpl();
    _collection.setRegistry(_registry);
    _collection.setUpdateFrequency(1);
    _collection.setServiceInterface(SimpleFederatedService.class);

    _collection.start();
  }

  @After
  public void stop() throws Exception {
    _collection.stop();
    if (_server != null)
      _server.stop();
  }

  @Test
  public void test() throws Exception {

    Set<FederatedService> services = _collection.getAllServices();
    assertTrue(services.isEmpty());

    _registry.addService("http://localhost:" + PORT + "/service-A/service",
        SimpleFederatedService.class.getName(), new HashMap<String, String>());

    Thread.sleep(TIMEOUT);

    services = _collection.getAllServices();
    assertEquals(1, services.size());

    SimpleFederatedService service = (SimpleFederatedService) _collection.getServiceForAgencyId("A");
    assertEquals("A", service.getValueForId(""));

    _registry.addService("http://localhost:" + PORT + "/service-B/service",
        SimpleFederatedService.class.getName(), new HashMap<String, String>());

    Thread.sleep(TIMEOUT);

    services = _collection.getAllServices();
    assertEquals(2, services.size());

    service = (SimpleFederatedService) _collection.getServiceForAgencyId("A");
    assertEquals("A", service.getValueForId(""));

    service = (SimpleFederatedService) _collection.getServiceForAgencyId("B");
    assertEquals("B", service.getValueForId(""));

    // Simulate a server crash
    _server.stop();
    _server = null;

    Thread.sleep(TIMEOUT);

    services = _collection.getAllServices();
    assertTrue(services.isEmpty());
  }

  private SimpleFederatedServiceImpl addServiceServlet(Server server,
      String agencyId, CoordinateBounds bounds) {
    Map<String, List<CoordinateBounds>> agenciesA = new HashMap<String, List<CoordinateBounds>>();
    agenciesA.put(agencyId, Arrays.asList(bounds));

    SimpleFederatedServiceImpl serviceA = new SimpleFederatedServiceImpl(
        agenciesA, agencyId);

    HessianServlet servletA = new HessianServlet();
    servletA.setHome(serviceA);
    servletA.setHomeAPI(SimpleFederatedService.class);

    Context contextA = new Context(server, "/service-" + agencyId,
        Context.SESSIONS);
    contextA.addServlet(new ServletHolder(servletA), "/*");

    return serviceA;
  }
}
