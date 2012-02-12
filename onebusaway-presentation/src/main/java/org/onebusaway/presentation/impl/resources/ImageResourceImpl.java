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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeUri;

public class ImageResourceImpl extends ResourcePrototypeImpl implements
    ImageResource, ResourceWithUrl, LocalResource {

  private String _url;

  public ImageResourceImpl(ClientBundleContext context,
      ClientBundleImpl parentBundle, String name, URL localUrl) {
    super(context, parentBundle, name);
    _localUrl = localUrl;
  }

  public String getUrl() {

    if (_url == null)
      refresh();

    return _url;
  }

  @Override
  public int getHeight() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int getLeft() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int getTop() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public String getURL() {
    return getUrl();
  }

  @Override
  public int getWidth() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public boolean isAnimated() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public SafeUri getSafeUri() {
    // TODO Auto-generated method stub
    return null;
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
