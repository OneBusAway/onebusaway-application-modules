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
package org.onebusaway.webapp.impl.resources;

import org.onebusaway.webapp.services.resources.DataResource;
import org.onebusaway.webapp.services.resources.ImmutableResourceBundleContext;
import org.onebusaway.webapp.services.resources.LocalResource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class DataResourceImpl extends ResourcePrototypeImpl implements
    DataResource, LocalResource {

  private String _url;

  public DataResourceImpl(ImmutableResourceBundleContext context,
      ImmutableResourceBundleImpl parentBundle, String name, URL localUrl) {
    super(context, parentBundle, name);
    _localUrl = localUrl;
  }

  public String getUrl() {

    if (_url == null)
      refresh();

    return _url;
  }

  @Override
  protected void refresh() {
    super.refresh();

    try {

      InputStream is = _localUrl.openStream();
      String key = ResourceSupport.getHash(is);
      is.close();

      String extension = getExtension();

      _url = constructURL(key, extension, this);

    } catch (IOException ex) {
      throw new IllegalStateException(ex);
    }

  }

  /*****************************************************************************
   * Private Methods
   ****************************************************************************/

  private String getExtension() {
    String path = _localUrl.getPath();
    int index = path.lastIndexOf('.');
    if (index != -1)
      return path.substring(index + 1);
    return "";
  }

}
