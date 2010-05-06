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

import edu.washington.cs.rse.transit.web.oba.gwt.resources.DataResource;
import edu.washington.cs.rse.transit.web.oba.gwt.resources.LocalResource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class DataResourceImpl extends ResourcePrototypeImpl implements
    DataResource, LocalResource {

  private URL _localURL;

  public DataResourceImpl(ImmutableResourceBundleImpl bundle, String name,
      URL localURL) {
    super(bundle, name);
    _localURL = localURL;
  }

  public String getUrl() {

    try {

      InputStream is = _localURL.openStream();
      String key = ResourceSupport.getHash(is);
      is.close();

      String extension = getExtension();

      return constructURL(key, extension);

    } catch (IOException ex) {
      throw new IllegalStateException(ex);
    }
  }

  /*****************************************************************************
   * {@link LocalResource} Interface
   ****************************************************************************/

  public URL getLocalUrl() {
    return _localURL;
  }

  public String getRemoteUrl() {
    return getUrl();
  }

  /*****************************************************************************
   * Private Methods
   ****************************************************************************/

  private String getExtension() {
    String path = _localURL.getPath();
    int index = path.lastIndexOf('.');
    if (index != -1)
      return path.substring(index + 1);
    return "";
  }
}
