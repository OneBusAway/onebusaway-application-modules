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
package org.onebusaway.common.web.gwt.resources;

import org.onebusaway.common.web.gwt.resources.ImmutableResourceBundle.Resource;
import org.onebusaway.common.web.gwt.resources.impl.CssResourceImpl;
import org.onebusaway.common.web.gwt.resources.impl.DataResourceImpl;
import org.onebusaway.common.web.gwt.resources.impl.ImmutableResourceBundleImpl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

public class ImmutableResourceBundleFactory {

  private Map<String, ImmutableResourceBundle> _bundles = new HashMap<String, ImmutableResourceBundle>();

  private Map<LocalResource, String> _resourceToUrl = new HashMap<LocalResource, String>();

  private Map<String, LocalResource> _urlToResource = new HashMap<String, LocalResource>();

  private File _tempDir;

  private URLStrategy _urlStrategy;

  private Context _context = new Context();

  public ImmutableResourceBundleFactory(URLStrategy urlStrategy) {
    _urlStrategy = urlStrategy;
  }

  public void setTempDir(File tempDir) {
    _tempDir = new File(tempDir, "ImmutableResourceBundles");
    if (!_tempDir.exists())
      _tempDir.mkdirs();
  }

  public void addResource(ServletContext context, String key,
      Class<?> bundleType) throws IOException {

    if (!ImmutableResourceBundle.class.isAssignableFrom(bundleType))
      throw new IllegalArgumentException("class is not assignable to "
          + ImmutableResourceBundle.class + ": " + bundleType);

    ImmutableResourceBundleImpl bundle = new ImmutableResourceBundleImpl(key);

    for (Method method : bundleType.getMethods()) {

      String methodName = method.getName();
      Resource resource = method.getAnnotation(ImmutableResourceBundle.Resource.class);

      if (resource == null) {
        if (!(methodName.equals("getResource") || methodName.equals("getResources")))
          System.err.println("no Resource annotation found: " + methodName);
        continue;
      }

      String[] values = resource.value();
      if (values == null || values.length != 1)
        throw new IllegalStateException("no value");
      String resourceName = values[0];

      Class<?> returnType = method.getReturnType();

      String path = context.getRealPath(resourceName);
      URL localURL = context.getResource(resourceName);
      
      if( localURL == null)
        throw new IllegalStateException("could not find resource: " + resourceName);

      if (CssResource.class.isAssignableFrom(returnType)) {
        CssResourceImpl resourceImpl = new CssResourceImpl(_context, bundle,
            methodName, localURL);
        if (path != null)
          resourceImpl.setLocalFile(new File(path));
        bundle.addResource(resourceImpl);
      } else if (DataResource.class.isAssignableFrom(returnType)) {
        bundle.addResource(new DataResourceImpl(_context, bundle, methodName,
            localURL));
      }
    }

    ImmutableResourceBundle bundleProxy = (ImmutableResourceBundle) Proxy.newProxyInstance(
        bundleType.getClassLoader(), new Class[] {bundleType}, bundle);
    _bundles.put(key, bundleProxy);
  }

  public Map<String, ImmutableResourceBundle> getBundles() {
    return _bundles;
  }

  public LocalResource getResourceForExternalUrl(String externalUrl) {
    return _urlToResource.get(externalUrl);
  }

  private class Context implements ImmutableResourceBundleContext {

    public String addContext(String url) {
      return _urlStrategy.addContext(url);
    }

    public String handleResource(String bundleName, String resourceName,
        String resourceKey, String resourceExtension, LocalResource resource) {

      String url = _urlStrategy.construct(bundleName, resourceName,
          resourceKey, resourceExtension);
      String existingUrl = _resourceToUrl.get(resource);
      if (existingUrl != null)
        _urlToResource.remove(existingUrl);
      _resourceToUrl.put(resource, url);
      _urlToResource.put(url, resource);
      return url;
    }

    public File getTempDir() {
      return _tempDir;
    }

  }
}
