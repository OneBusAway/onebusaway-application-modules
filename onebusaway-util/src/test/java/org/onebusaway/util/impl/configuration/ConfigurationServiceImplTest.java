/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
package org.onebusaway.util.impl.configuration;

import static org.junit.Assert.*;

import org.onebusaway.container.refresh.RefreshService;
import org.onebusaway.util.rest.RestApiLibrary;
import org.onebusaway.util.services.configuration.ConfigurationServiceClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.HashMap;

public class ConfigurationServiceImplTest {

  @SuppressWarnings("unused")
  @Mock
  private RefreshService refreshService;

  @Mock
  private ConfigurationServiceClient mockApiLibrary;

  @Mock
  private RestApiLibrary mockRestApiLibrary;


  private ConfigurationServiceImpl service;
  private ConfigurationServiceClientFileImpl client;
  private HashMap<String, Object> map = new HashMap<>();

  @Test
  public void noop()  {
  }

@Before
  public void setUp() throws Exception {
    service = new ConfigurationServiceImpl();
    client = new ConfigurationServiceClientFileImpl();
    client.setConfig(map);
    service.setConfigurationServiceClient(client);
  }

  @Test
  public void testGetConfigurationFlagForAgency() throws Exception {
    assertNotNull("service cannot be null", service);
    boolean hideScheduleInfo = service.getConfigurationFlagForAgency("1", "hideScheduleInfo");
    assertEquals("expected false", false, hideScheduleInfo);
    ArrayList settings = new ArrayList();
    addToSettings(settings, "agency_1", "hideScheduleInfo", "true");
    map.put("config", settings);

    hideScheduleInfo = service.getConfigurationFlagForAgency("1", "hideScheduleInfo");
    assertEquals("expected true", true, hideScheduleInfo);
  }

  private void addToSettings(ArrayList settings, String component, String key, String value) {
    HashMap<String, String> kv1 = new HashMap<>();
    // for agency configuration the component is "agency_" + agency_id
    kv1.put("component", component);
    kv1.put("key", key);
    kv1.put("value", value);
    settings.add(kv1);
  }

}
