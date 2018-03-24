/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2012 Google, Inc.
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
package org.onebusaway.transit_data.model.config;

public class BundleMetadata implements java.io.Serializable {

  private static final long serialVersionUID = -7693328387231949115L;
  
  private String id;
  private String name;
  private String serviceDateFrom;
  private String serviceDateTo;
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
  
}
