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

import org.onebusaway.webapp.services.resources.CssResource;
import org.onebusaway.webapp.services.resources.DataResource;
import org.onebusaway.webapp.services.resources.ImmutableResourceBundle;
import org.onebusaway.webapp.services.resources.ImmutableResourceBundleContext;
import org.onebusaway.webapp.services.resources.LocalResource;
import org.onebusaway.webapp.services.resources.ImmutableResourceBundle.Resource;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

public class ImmutableResourceBundleFactory {

  private Map<String, ImmutableResourceBundle> _bundles = new HashMap<String, ImmutableResourceBundle>();

  private Map<LocalResource, String> _resourceToUrl = new HashMap<LocalResource, String>();

  private Map<String, LocalResource> _urlToResource = new HashMap<String, LocalResource>();

  private String _prefix;

  private File _tempDir;

  private ContextImpl _context = new ContextImpl();

  private ServletContext _servletContext;

  private String _contextPath;

  public void setPrefix(String prefix) {
    _prefix = prefix;
  }

  public void setServletContext(ServletContext servletContext) {

    _servletContext = servletContext;
    _contextPath = getContextPath(_servletContext);

    File tmpDir = (File) _servletContext.getAttribute("javax.servlet.context.tempdir");
    if (tmpDir == null) {
      System.err.println("NO ServletContext TEMP DIR!");
      tmpDir = new File(System.getProperty("java.io.tmpdir"));
    }

    _tempDir = new File(tmpDir, "ImmutableResourceBundles");
    if (!_tempDir.exists())
      _tempDir.mkdirs();

    _servletContext.setAttribute("resources", _bundles);
  }

  public void setResources(Map<String, Class<?>> resources) throws IOException {
    for (Map.Entry<String, Class<?>> entry : resources.entrySet()) {
      String key = entry.getKey();
      Class<?> bundleType = entry.getValue();
      addResource(key, bundleType);
    }
  }

  public void addResource(String key, Class<?> bundleType) throws IOException {

    if (!ImmutableResourceBundle.class.isAssignableFrom(bundleType))
      throw new IllegalArgumentException("class is not assignable to "
          + ImmutableResourceBundle.class + ": " + bundleType);

    try {
      ImmutableResourceBundleImpl bundle = new ImmutableResourceBundleImpl(key);

      processBundleMethods(bundleType, bundle);

      bundle.refreshAll();

      ImmutableResourceBundle bundleProxy = (ImmutableResourceBundle) Proxy.newProxyInstance(
          bundleType.getClassLoader(), new Class[] {bundleType}, bundle);
      _bundles.put(key, bundleProxy);
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new IllegalStateException("error constructing bundle " + key
          + " of type + " + bundleType, ex);
    }
  }

  public Map<String, ImmutableResourceBundle> getBundles() {
    return _bundles;
  }

  public LocalResource getResourceForExternalUrl(String externalUrl) {
    return _urlToResource.get(externalUrl);
  }

  /****
   * Private Methods
   ****/

  private void processBundleMethods(Class<?> bundleType,
      ImmutableResourceBundleImpl bundle) throws MalformedURLException {
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

      String path = _servletContext.getRealPath(resourceName);
      URL localURL = _servletContext.getResource(resourceName);

      System.out.println("path=" + path);
      System.out.println("localUrl=" + localURL);

      if (localURL == null)
        throw new IllegalStateException("could not find resource: "
            + resourceName);

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
  }

  private String construct(String bundleName, String resourceName,
      String resourceKey, String resourceExtension) {

    StringBuilder b = new StringBuilder();

    /*
     * if (_prefix != null && _prefix.length() > 0) b.append(_prefix);
     */

    b.append('/');
    b.append(bundleName);
    b.append('/');
    if (resourceName.startsWith("get"))
      resourceName = resourceName.substring(3);
    b.append(resourceName);
    b.append('-');
    b.append(resourceKey);

    if (resourceExtension != null && resourceExtension.length() > 0)
      b.append('.').append(resourceExtension);

    return b.toString();
  }

  private String addContext(String url) {
    if (_contextPath != null && _contextPath.length() > 0)
      url = _contextPath + url;
    return url;
  }

  private String getContextPath(ServletContext context) {
    // Get the context path without the request.
    String contextPath = "";
    try {
      String path = context.getResource("/").getPath();
      contextPath = path.substring(0, path.lastIndexOf("/"));
      contextPath = contextPath.substring(contextPath.lastIndexOf("/"));
      if (contextPath.equals("/localhost"))
        contextPath = "";
    } catch (Exception e) {
      e.printStackTrace();
    }
    return contextPath;
  }

  /****
   * 
   ****/

  private class ContextImpl implements ImmutableResourceBundleContext {

    public String addContext(String url) {
      return ImmutableResourceBundleFactory.this.addContext(url);
    }

    public String handleResource(String bundleName, String resourceName,
        String resourceKey, String resourceExtension, LocalResource resource) {

      String url = construct(bundleName, resourceName, resourceKey,
          resourceExtension);
      String existingUrl = _resourceToUrl.get(resource);
      if (existingUrl != null)
        _urlToResource.remove(existingUrl);
      _resourceToUrl.put(resource, url);
      _urlToResource.put(url, resource);

      if (_prefix != null)
        url = _prefix + url;

      return url;
    }

    public File getTempDir() {
      return _tempDir;
    }
  }
}
