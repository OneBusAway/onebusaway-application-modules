/*
 * Copyright 2008 Brian Ferris
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.washington.cs.rse.transit.web.oba.gwt.resources.impl;

import edu.washington.cs.rse.transit.web.oba.gwt.resources.ResourcePrototype;

public class ResourcePrototypeImpl implements ResourcePrototype {

  protected ImmutableResourceBundleImpl _resource;

  private String _name;

  public ResourcePrototypeImpl(ImmutableResourceBundleImpl resource, String name) {
    _resource = resource;
    _name = name;
  }

  public String getName() {
    return _name;
  }

  protected String constructURL(String key, String extension) {
    return _resource.constructURL(_name, key, extension);
  }

  protected String addContext(String url) {
    return _resource.addContext(url);
  }
}
