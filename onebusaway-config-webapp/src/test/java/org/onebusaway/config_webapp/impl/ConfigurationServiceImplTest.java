/**
 * Copyright (C) 2011 Metropolitan Transportation Authority
 * Copyright (C) 2012 Kurt Raschke
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

package org.onebusaway.config_webapp.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationServiceImplTest {
    
  @InjectMocks
  private ConfigurationServiceImpl service;

  @Test
  public void testDefaultvalue() throws Exception {
    assertEquals(service.getConfigurationValueAsString("test789", "default"), "default");
  }

  @Test
  public void setValue() throws Exception {
    service.setConfigurationValue("testComponent", "test123", "testValue");
    assertEquals(service.getConfigurationValueAsString("test123", null), "testValue");
  }
}
