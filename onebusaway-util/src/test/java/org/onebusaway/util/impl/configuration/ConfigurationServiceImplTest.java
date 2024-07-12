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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.onebusaway.container.refresh.RefreshService;
import org.onebusaway.util.rest.RestApiLibrary;
import org.onebusaway.util.services.configuration.ConfigurationServiceClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

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

  @Test
  public void testMergeConfig(){
    HashMap<String, Object> staticContent = new HashMap<>();
    ArrayList<HashMap> items = new ArrayList<>();
    HashMap<String, String> item1 = new HashMap<>();
    item1.put("component", "admin");
    item1.put("key", "test");
    item1.put("value","static");
    items.add(item1);

    item1 = new HashMap<>();
    item1.put("component", "admin");
    item1.put("key", "staticOnly");
    item1.put("value","static");
    items.add(item1);

    staticContent.put("config", items);

    HashMap<String, Object> dynamicContent = new HashMap<>();
    HashMap<String, String> item2 = new HashMap<>();
    items = new ArrayList<>();
    item2.put("component", "admin");
    item2.put("key", "test");
    item2.put("value","dynamic");
    items.add(item2);

    item2 = new HashMap<>();
    item2.put("component", "admin");
    item2.put("key", "dynamicOnly");
    item2.put("value","dynamic");
    items.add(item2);
    dynamicContent.put("config", items);

    HashMap<String, Object> mergeConfig = client.mergeConfig(staticContent, dynamicContent);
    ArrayList<HashMap<String, String>> mergedItems = (ArrayList) mergeConfig.get("config");
    boolean found1 = false;
    boolean found2 = false;
    boolean found3 = false;

    for (HashMap<String, String> mergedItem : mergedItems) {
      String actualKey = client.getItemKey(mergedItem);
      if ("admin.staticOnly".equals(actualKey)) {
        found1 = true;
        assertEquals("static", mergedItem.get("value"));
      } else if ("admin.dynamicOnly".equals(actualKey)) {
        found2 = true;
        assertEquals("dynamic", mergedItem.get("value"));
      } else if ("admin.test".equals(actualKey)) {
        found3 = true;
        assertEquals("dynamic", mergedItem.get("value"));
      } else {
        fail("unexpected key=" +actualKey);
      }
    }
    assertTrue(found1);
    assertTrue(found2);
    assertTrue(found3);
  }

  @Test
  public void testGetConfigFromApi() throws URISyntaxException, MalformedURLException {
    URI uri = getClass().getResource("config.json").toURI();
    String url = uri.toURL().toString();
    client.setExternalConfigurationApiUrl(url);
    HashMap<String, Object> results = client.getConfigFromApi();
    assertNotNull(results);
    ArrayList<HashMap<String, String>> configList = (ArrayList<HashMap<String, String>>) results.get("config");
    HashMap<String, String> item1 = configList.get(0);
    assertEquals("tdm", item1.get("component"));
    assertEquals("display.minimumValue", item1.get("key"));
    assertEquals("false", item1.get("value"));
  }

  private void addToSettings(ArrayList settings, String component, String key, String value) {
    HashMap<String, String> kv1 = new HashMap<>();
    // for agency configuration the component is "agency_" + agency_id
    kv1.put("component", component);
    kv1.put("key", key);
    kv1.put("value", value);
    settings.add(kv1);
  }

  private static class ConfigItem{
    public String component;
    public String key;
    public String value;

    public ConfigItem(String c, String k, String v){
      this.component = c;
      this.key = k;
      this.value = v;
    }
    public ConfigItem(){

    }
  }
  private static class ConfigFileStructure{
    public HashMap<String, String> oba;
    public ArrayList<ConfigItem> config;
  }

}
