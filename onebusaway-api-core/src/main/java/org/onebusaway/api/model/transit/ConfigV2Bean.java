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
package org.onebusaway.api.model.transit;

import java.io.Serializable;
import java.util.Properties;

public class ConfigV2Bean implements Serializable, HasId {
  
  private String id;
  private String name;
  private String serviceDateFrom;
  private String serviceDateTo;
  private Properties gitProperties;
  
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getServiceDateFrom() {
    return serviceDateFrom;
  }
  public void setServiceDateFrom(String serviceDateFrom) {
    this.serviceDateFrom = serviceDateFrom;
  }
  public String getServiceDateTo() {
    return serviceDateTo;
  }
  public void setServiceDateTo(String serviceDateTo) {
    this.serviceDateTo = serviceDateTo;
  }
  public Properties getGitProperties() {
    return gitProperties;
  }
  public void setGitProperties(Properties gitProperties) {
    this.gitProperties = gitProperties;
  }
  

}
