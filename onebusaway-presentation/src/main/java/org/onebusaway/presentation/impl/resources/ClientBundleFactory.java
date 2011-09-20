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

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ResourcePrototype;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.onebusaway.presentation.services.resources.WebappSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientBundleFactory {

  private static final String PREFIX_CLASSPATH = "classpath:";

  private static Logger _log = LoggerFactory.getLogger(ClientBundleFactory.class);

  private Map<String, Object> _bundlesByNamePath = new HashMap<String, Object>();

  private Map<Class<?>, ClientBundle> _bundlesByType = new HashMap<Class<?>, ClientBundle>();

  private Map<LocalResource, String> _resourceToUrl = new HashMap<LocalResource, String>();

  private Map<String, LocalResource> _urlToResource = new HashMap<String, LocalResource>();

  private String _prefix;
  
  private String _pattern;

  private File _tempDir;

  private ContextImpl _context = new ContextImpl();

  private ServletContext _servletContext;

  private String _contextPath;

  public void setPrefix(String prefix) {
    _prefix = prefix;
  }
  
  public void setPattern(String pattern) {
    _pattern = pattern;
  }

  public void setServletContext(ServletContext servletContext) {

    _servletContext = servletContext;
    _contextPath = getContextPath(_servletContext);

    File tmpDir = (File) _servletContext.getAttribute("javax.servlet.context.tempdir");
    if (tmpDir == null) {
      _log.warn("NO ServletContext TEMP DIR!");
      tmpDir = new File(System.getProperty("java.io.tmpdir"));
    }

    _tempDir = new File(tmpDir, "ClientBundles");
    if (!_tempDir.exists())
      _tempDir.mkdirs();

    _servletContext.setAttribute("resources", _bundlesByNamePath);
  }

  public void setResources(List<Class<?>> resources) throws IOException {

    for (Class<?> bundleType : resources) {
      try {
        addResource(bundleType);
      } catch (Exception ex) {
        _log.error("error adding resource of type: " + bundleType.getName(), ex);
        throw new IllegalStateException("error adding resource of type: "
            + bundleType.getName(), ex);
      }
    }
  }

  public void addResource(Class<?> bundleType) throws IOException {

    if (!ClientBundle.class.isAssignableFrom(bundleType))
      throw new IllegalArgumentException("class is not assignable to "
          + ClientBundle.class + ": " + bundleType);

    // Have we already seen this bundle?
    if (_bundlesByType.containsKey(bundleType))
      return;

    ClientBundleImpl bundle = new ClientBundleImpl(bundleType);

    List<ResourcePrototypeImpl> resources = processBundleMethods(bundleType,
        bundle);

    for (ResourcePrototypeImpl resource : resources)
      resource.refresh();

    ClientBundle bundleProxy = (ClientBundle) Proxy.newProxyInstance(
        bundleType.getClassLoader(), new Class[] {bundleType}, bundle);
    _bundlesByType.put(bundleType, bundleProxy);

    addBundleToResourcePath(bundleType, bundleProxy);
  }

  public Map<String, Object> getBundles() {
    return _bundlesByNamePath;
  }

  public LocalResource getResourceForExternalUrl(String externalUrl) {
    return _urlToResource.get(externalUrl);
  }

  @SuppressWarnings("unchecked")
  public <T extends ClientBundle> T getBundleForType(Class<T> bundleType) {
    ClientBundle bundle = _bundlesByType.get(bundleType);
    return (T) bundle;
  }

  /****
   * Private Methods
   * 
   * @return
   ****/

  private List<ResourcePrototypeImpl> processBundleMethods(Class<?> bundleType,
      ClientBundleImpl bundle) throws MalformedURLException {

    List<ResourcePrototypeImpl> resources = new ArrayList<ResourcePrototypeImpl>();

    for (Method method : bundleType.getMethods()) {

      String methodName = method.getName();
      WebappSource source = method.getAnnotation(WebappSource.class);

      if (source == null) {
        _log.warn("no Resource annotation found: " + methodName);
        continue;
      }

      String[] values = source.value();
      if (values == null || values.length != 1)
        throw new IllegalStateException("@WebappSource has no value: "
            + bundleType.getName() + "#" + methodName);

      String resourceName = values[0];

      Class<?> returnType = method.getReturnType();

      URL localURL = getBundleResourceAsLocalUrl(bundleType, resourceName);

      if (localURL == null) {
        _log.warn("could not find ClientBundle resource for "
            + bundleType.getName() + "#" + methodName + " source="
            + resourceName);
        continue;
      }

      File localFile = getBundleResourceAsLocalFile(resourceName, localURL);

      String name = getMethodNameAsPropertyName(methodName);

      if (name == null) {
        _log.warn("invalid resource name: " + bundleType.getName() + "#"
            + methodName);
        continue;
      }

      if (CssResource.class.isAssignableFrom(returnType)) {

        CssResourceImpl resourceImpl = new CssResourceImpl(_context, bundle,
            methodName, localURL);
        if (localFile != null)
          resourceImpl.setLocalFile(localFile);
        resources.add(resourceImpl);

        ResourcePrototype resourceProxy = (ResourcePrototype) Proxy.newProxyInstance(
            bundleType.getClassLoader(), new Class[] {
                returnType, ResourceWithUrl.class}, resourceImpl);
        bundle.addResource(resourceProxy);

      } else if (ImageResource.class.isAssignableFrom(returnType)) {
        ImageResourceImpl resourceImpl = new ImageResourceImpl(_context,
            bundle, methodName, localURL);
        bundle.addResource(resourceImpl);
        resources.add(resourceImpl);
      }
    }

    return resources;
  }

  private URL getBundleResourceAsLocalUrl(Class<?> bundleType,
      String resourceName) throws MalformedURLException {

    if (resourceName.startsWith(PREFIX_CLASSPATH)) {
      resourceName = resourceName.substring(PREFIX_CLASSPATH.length());
      URL resource = bundleType.getResource(resourceName);
      if( resource == null)
        _log.warn("unknown classpath resource: name=" + resourceName + " bundleType=" + bundleType.getName());
      return resource;
    }

    return _servletContext.getResource(resourceName);
  }

  private File getBundleResourceAsLocalFile(String resourceName, URL resourceUrl) {
    String protocol = resourceUrl.getProtocol();
    if ("file".equals(protocol))
      return new File(resourceUrl.getPath());
    File path = new File(_servletContext.getRealPath(resourceName));
    if (path.exists())
      return path;
    return null;
  }

  private String construct(String bundleName, String resourceName,
      String resourceKey, String resourceExtension) {

    StringBuilder b = new StringBuilder();

    b.append(bundleName);
    b.append('-');
    if (resourceName.startsWith("get"))
      resourceName = resourceName.substring(3);
    b.append(resourceName);
    b.append('-');
    b.append(resourceKey);
    b.append(".cache");

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

  private String getMethodNameAsPropertyName(String methodName) {
    if (!methodName.startsWith("get"))
      return methodName;

    String name = methodName.substring(3);
    if (name.length() == 0)
      return null;

    return name.substring(0, 1).toLowerCase() + name.substring(1);
  }

  @SuppressWarnings("unchecked")
  private void addBundleToResourcePath(Class<?> bundleType,
      ClientBundle bundleProxy) {

    List<String> keys = getKeysForBundleType(bundleType);
    for (String key : keys) {

      Map<String, Object> current = _bundlesByNamePath;
      String[] tokens = key.split("\\.");

      for (int i = 0; i < tokens.length - 1; i++) {
        Object obj = current.get(tokens[i]);
        if (obj == null)
          obj = new HashMap<String, Object>();
        if (!(obj instanceof Map<?, ?>))
          throw new IllegalStateException("name collision: " + key
              + " bundleType=" + bundleType.getName());
        current = (Map<String, Object>) obj;
      }

      current.put(tokens[tokens.length - 1], bundleProxy);
    }

  }

  private List<String> getKeysForBundleType(Class<?> bundleType) {
    List<String> names = new ArrayList<String>();
    String name = bundleType.getName();
    names.add(name);
    int index = name.lastIndexOf(".");
    if (index != -1) {
      name = name.substring(index + 1);
      if (name.length() > 0)
        names.add(name);
    }
    return names;
  }

  /****
   * 
   ****/

  private class ContextImpl implements ClientBundleContext {

    public String addContext(String url) {
      return ClientBundleFactory.this.addContext(url);
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
      
      if( _pattern != null)
        url = _pattern.replaceAll("\\{\\}", url);

      if (_prefix != null)
        url = _prefix + url;

      if (_contextPath != null)
        url = _contextPath + url;

      return url;
    }

    public File getTempDir() {
      return _tempDir;
    }
  }
}
