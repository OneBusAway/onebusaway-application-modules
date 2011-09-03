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
package org.onebusaway.presentation.impl.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;

import org.onebusaway.presentation.services.resources.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;

public class ResourceFactory {

  private ResourceService _resourceService;

  private String _name;

  private List<String> _resources = new ArrayList<String>();

  private Locale _locale;

  @Autowired
  public void setResourceService(ResourceService resourceService) {
    _resourceService = resourceService;
  }

  public void setName(String name) {
    _name = name;
  }

  public void setResource(String resource) {
    _resources = Arrays.asList(resource);
  }

  public void setResources(List<String> resources) {
    _resources = resources;
  }
  
  public void setLocale(Locale locale) {
    _locale = locale;
  }

  @PostConstruct
  public void setup() {
    Locale locale = Locale.getDefault();
    if( _locale != null )
      locale = _locale;
    _resourceService.getExternalUrlForResources(_name, _resources, locale);
  }
}
