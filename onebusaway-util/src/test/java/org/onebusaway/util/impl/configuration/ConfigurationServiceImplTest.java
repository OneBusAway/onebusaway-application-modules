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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.onebusaway.container.refresh.RefreshService;
import org.onebusaway.util.rest.RestApiLibrary;
import org.onebusaway.util.services.configuration.ConfigurationServiceClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
    HashMap<String, Object> old = new HashMap<>();
    ArrayList<HashMap> items = new ArrayList<>();
    HashMap<String, String> item1 = new HashMap<>();
    item1.put("component", "admin");
    item1.put("key", "test");
    item1.put("value","hello World");
    items.add(item1);
    old.put("config", items);

  }

  @Test
  public void testReadConfig(){
    ObjectMapper mapper = new ObjectMapper();
    try {

      String jsonString = "{\"oba\":{\"env\":\"jared\"},\"config\":[{\"component\":\"testing\",\"key\":\"jared\",\"value\":\"hello world!\"}]}";
      ConfigFileStructure cfs = mapper.readValue(new File("/opt/nyc/oba/config.json"), ConfigFileStructure.class);


      // compact print
      System.out.println(cfs);

      // pretty print
      String prettyFile = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(cfs);

      System.out.println(prettyFile);


    } catch (IOException e) {
      e.printStackTrace();
    }
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
