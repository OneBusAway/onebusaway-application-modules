/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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

import java.util.List;
import java.util.Map;

public class ConfigFileStructure {

  private Map<String, String> oba;
  private Map<String, List<ConfigParameter>> agencies;
  private List<ConfigItem> config;

  public Map<String, String> getOba() {
    return oba;
  }

  public void setOba(Map<String, String> oba) {
    this.oba = oba;
  }

  public Map<String, List<ConfigParameter>> getAgencies() {
    return agencies;
  }

  public void setAgencies(Map<String, List<ConfigParameter>> agencies) {
    this.agencies = agencies;
  }

  public List<ConfigItem> getConfig() {
    return config;
  }

  public void setConfig(List<ConfigItem> config) {
    this.config = config;
  }

}
