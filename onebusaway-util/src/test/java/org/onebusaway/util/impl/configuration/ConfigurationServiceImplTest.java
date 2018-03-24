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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.onebusaway.container.refresh.RefreshService;
import org.onebusaway.util.impl.configuration.ConfigurationServiceImpl;
import org.onebusaway.util.rest.RestApiLibrary;
import org.onebusaway.util.services.configuration.ConfigurationServiceClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URL;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationServiceImplTest {

  @SuppressWarnings("unused")
  @Mock
  private RefreshService refreshService;

  @Mock
  private ConfigurationServiceClient mockApiLibrary;

  @Mock
  private RestApiLibrary mockRestApiLibrary;

  @InjectMocks
  private ConfigurationServiceImpl service;

  @Test
  public void noop()  {
  }

  /*@Before
  public void setupApiLibrary() throws Exception {
    RestApiLibrary ral = new RestApiLibrary("localhost", null, "api");    
    String json = new String("{\"config\":[{\"value\":\"20\",\"key\":\"tdm.crewAssignmentRefreshInterval\",\"description\":null,\"value-type\":\"String\",\"units\":\"minutes\",\"component\":\"tdm\",\"updated\":null}],\"status\":\"OK\"}");
    when(mockApiLibrary.getItemsForRequest("config", "list"))
      .thenReturn(ral.getJsonObjectsForString(json));

    ConfigurationServiceClient tdmal = new ConfigurationServiceClientTDMImpl("tdm.staging.obanyc.com", 80, "/api");
    URL setUrl = tdmal.buildUrl("config", "testComponent", "test123", "set");
    when(mockRestApiLibrary.setContents(setUrl, "testValue"))
      .thenReturn(true);

    service.refreshConfiguration();
  }

  @Test
  public void testDefaultvalue() throws Exception {
    assertEquals(service.getConfigurationValueAsString("test789", "default"), "default");
  }

  @Test
  public void testGetExistingValueFromTDM() throws Exception {
    assertEquals(service.getConfigurationValueAsString("tdm.crewAssignmentRefreshInterval", null), "20");
  }

  @Test
  public void testGetExistingValueFromTDMDefaultOverridden() throws Exception {
    assertEquals(service.getConfigurationValueAsString("tdm.crewAssignmentRefreshInterval", "30"), "20");
  }

  @Test
  public void setValue() throws Exception {
    service.setConfigurationValue("testComponent", "test123", "testValue");
    assertEquals(service.getConfigurationValueAsString("test123", null), "testValue");
  }*/
}
