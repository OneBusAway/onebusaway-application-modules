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
package org.onebusaway.common.web.gwt.resources.impl;

import org.onebusaway.common.web.gwt.resources.ImmutableResourceBundleContext;
import org.onebusaway.common.web.gwt.resources.LocalResource;
import org.onebusaway.common.web.gwt.resources.ResourcePrototype;

import java.io.File;
import java.net.URL;

public class ResourcePrototypeImpl implements ResourcePrototype {

  protected ImmutableResourceBundleContext _context;

  protected ImmutableResourceBundleImpl _parentBundle;

  private String _name;

  protected boolean _initialized = false;
  protected URL _localUrl;
  protected File _localFile = null;
  private long _lastModified = System.currentTimeMillis();
  protected long _localFileLength = -1;

  public ResourcePrototypeImpl(ImmutableResourceBundleContext context,
      ImmutableResourceBundleImpl parentBundle, String name) {
    _context = context;
    _parentBundle = parentBundle;
    _name = name;
  }

  public String getName() {
    return _name;
  }

  public void setLocalFile(File file) {
    _localFile = file;
  }

  /*****************************************************************************
   * {@link LocalResource} Interface
   ****************************************************************************/

  public URL getLocalUrl() {
    refresh();
    return _localUrl;
  }

  public long getLastModifiedTime() {
    refresh();
    return _lastModified;
  }

  /*****************************************************************************
   * Protected Methods
   ****************************************************************************/

  protected boolean isUpToDate() {
    if (!_initialized)
      return false;
    if (_localFile != null
        && (_localFile.lastModified() != _lastModified || _localFile.length() != _localFileLength))
      return false;
    return true;
  }

  protected void setUpToDate() {
    _initialized = true;
    if (_localFile != null) {
      _lastModified = _localFile.lastModified();
      _localFileLength = _localFile.length();
    }
  }

  protected void refresh() {

  }

  protected String constructURL(String resourceKey, String resourceExtension,
      LocalResource resource) {
    return _context.handleResource(_parentBundle.getName(), _name, resourceKey,
        resourceExtension, resource);
  }
}
