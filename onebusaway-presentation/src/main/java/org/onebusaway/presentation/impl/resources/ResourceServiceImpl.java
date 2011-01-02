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
package org.onebusaway.presentation.impl.resources;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import org.json.JSONObject;
import org.onebusaway.presentation.impl.ServletLibrary;
import org.onebusaway.presentation.services.resources.ResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

@Component
public class ResourceServiceImpl implements ResourceService {

  public static final String DEBUG_PROPERTY = ResourceServiceImpl.class.getName()
      + ".debug";

  private static final String PREFIX_CLASSPATH = "classpath:";

  private static final String PREFIX_COLLECTION = "collection:";

  private static final Pattern _resourcePattern = Pattern.compile("^(.*)-\\w+\\.cache(\\.\\w+){0,1}$");

  private static Logger _log = LoggerFactory.getLogger(ResourceServiceImpl.class);

  private ConcurrentMap<String, Resource> _resourceEntriesByResourcePath = new ConcurrentHashMap<String, Resource>();

  private ConcurrentMap<String, Resource> _resourceEntriesByExternalId = new ConcurrentHashMap<String, Resource>();

  /****
   * 
   ****/

  private String _prefix;

  private String _pattern;

  private File _tempDir;

  private ServletContext _servletContext;

  private String _contextPath;

  private boolean _debug = false;

  public void setPrefix(String prefix) {
    _prefix = prefix;
  }

  public void setPattern(String pattern) {
    _pattern = pattern;
  }

  @Autowired
  public void setServletContext(ServletContext servletContext) {

    _servletContext = servletContext;
    _contextPath = ServletLibrary.getContextPath(_servletContext);

    File tmpDir = (File) _servletContext.getAttribute("javax.servlet.context.tempdir");
    if (tmpDir == null) {
      _log.warn("NO ServletContext TEMP DIR!");
      tmpDir = new File(System.getProperty("java.io.tmpdir"));
    }

    _tempDir = new File(tmpDir, "OneBusAwayResources");
    if (!_tempDir.exists())
      _tempDir.mkdirs();
  }

  @PostConstruct
  public void setup() {
    String value = System.getProperty(DEBUG_PROPERTY, "false");
    if (value != null && value.toLowerCase().equals("true"))
      _debug = true;
  }

  /****
   * {@link ResourceService} Interface
   ****/

  @Override
  public String getExternalUrlForResource(String resourcePath) {

    Resource resource = getResourceForPath(resourcePath, null);

    if (resource == null) {
      _log.warn("resource not found: " + resourcePath);
      return null;
    }

    refreshResource(resource);

    return resource.getExternalUrl();
  }

  @Override
  public String getExternalUrlForResources(List<String> resourcePaths) {
    return getExternalUrlForResources(null, resourcePaths);
  }

  @Override
  public String getExternalUrlForResources(String resourceId,
      List<String> resourcePaths) {

    Resource resource = getResourceForPaths(resourceId, resourcePaths);

    if (resource == null) {
      _log.warn("resource not found: " + resourceId);
      return null;
    }

    refreshResource(resource);

    return resource.getExternalUrl();
  }

  @Override
  public URL getLocalUrlForExternalId(String externalId) {
    Resource resource = _resourceEntriesByExternalId.get(externalId);
    if (resource == null) {
      /**
       * In case the resource has not been first requested as a resource(url)
       * first
       */
      String resourcePath = getExternalIdAsResourcePath(externalId);
      if (resourcePath != null)
        resource = getResourceForPath(resourcePath, null);
    }
    
    if (resource == null) {
      _log.warn("resource not found for external id: " + externalId);
      return null;
    }
    
    return resource.getLocalUrl();
  }

  /****
   * Private Methods
   ****/

  private Resource getResourceForPath(String resourcePath, URL sourceUrl) {

    Resource resource = _resourceEntriesByResourcePath.get(resourcePath);

    if (resource == null) {

      resource = createResourceForPath(resourcePath, sourceUrl);

      if (resource == null)
        return null;

      Resource existingResource = _resourceEntriesByResourcePath.putIfAbsent(
          resourcePath, resource);

      if (existingResource != null)
        return existingResource;
    }

    return resource;
  }

  private ResourceEntry createResourceForPath(String resourcePath, URL sourceUrl) {

    if (sourceUrl == null)
      sourceUrl = getResourceAsSourceUrl(resourcePath);

    /**
     * If we can't find a source URL, then we can't create an entry
     */
    if (sourceUrl == null) {
      return null;
    }

    File localFile = getBundleResourceAsLocalFile(resourcePath, sourceUrl);

    ResourceTransformationStrategy strategy = getResourceTransformationStrategyForResource(resourcePath);

    ResourceEntry resource = new ResourceEntry(resourcePath, sourceUrl,
        localFile, strategy);
    generateLocalResourceAndExternalUrl(resource);
    return resource;
  }

  private URL getResourceAsSourceUrl(String resourceName) {

    if (resourceName.startsWith(PREFIX_COLLECTION)) {
      resourceName = resourceName.substring(PREFIX_COLLECTION.length());
      return getCollectionResourceAsSourceUrl(resourceName);
    }

    if (resourceName.startsWith(PREFIX_CLASSPATH)) {
      resourceName = resourceName.substring(PREFIX_CLASSPATH.length());
      ClassLoader loader = getClass().getClassLoader();
      URL resource = loader.getResource(resourceName);
      if (resource == null)
        _log.warn("unknown classpath resource: name=" + resourceName);
      return resource;
    }

    try {
      return _servletContext.getResource(resourceName);
    } catch (MalformedURLException ex) {
      throw new IllegalStateException("error requesting servlet url: "
          + resourceName, ex);
    }

  }

  private URL getCollectionResourceAsSourceUrl(String resourceName) {

    int index = resourceName.indexOf('=');
    if (index == -1)
      throw new IllegalStateException("invalid resource collection specifier: "
          + resourceName);

    String collectionPrefix = resourceName.substring(0, index);
    String collectionResourcePath = resourceName.substring(index + 1);

    ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    Map<String, String> resourceMapping = new HashMap<String, String>();

    try {
      org.springframework.core.io.Resource[] resources = resolver.getResources(collectionResourcePath);

      for (org.springframework.core.io.Resource resource : resources) {
        URL url = resource.getURL();
        String name = getLocalUrlAsResourceName(url);
        Resource r = getResourceForPath(name, url);
        if (r != null) {
          String path = url.getPath();
          int sepIndex = path.lastIndexOf(File.separator);
          if (sepIndex != -1)
            path = path.substring(sepIndex + 1);
          resourceMapping.put(path, r.getExternalUrl());
        }
      }

      File file = getOutputFile(PREFIX_COLLECTION + collectionPrefix + ".js");
      PrintWriter out = new PrintWriter(file);
      JSONObject obj = new JSONObject(resourceMapping);
      out.println("var OBA = window.OBA || {};");
      out.println("if(!OBA.Resources) { OBA.Resources = {}; }");
      out.println("OBA.Resources." + collectionPrefix + " = " + obj.toString()
          + ";");
      out.close();

      return getFileAsUrl(file);

    } catch (IOException ex) {
      throw new IllegalStateException("error loading resources", ex);
    }

  }

  private File getBundleResourceAsLocalFile(String resourcePath, URL resourceUrl) {
    String protocol = resourceUrl.getProtocol();
    if ("file".equals(protocol))
      return new File(resourceUrl.getPath());
    File path = new File(_servletContext.getRealPath(resourcePath));
    if (path.exists())
      return path;
    return null;
  }

  private ResourceTransformationStrategy getResourceTransformationStrategyForResource(
      String resourcePath) {
    if (resourcePath.endsWith(".css"))
      return new CssResourceTransformationStrategy();
    return new DefaultResourceTransformationStrategy();
  }

  private boolean refreshResource(Resource resource) {

    if (resource instanceof ResourceEntry) {
      return refreshResourceEntry((ResourceEntry) resource);
    } else if (resource instanceof ResourcesEntry) {
      return refreshResourcesEntry((ResourcesEntry) resource);
    }

    return false;
  }

  private boolean refreshResourceEntry(ResourceEntry resource) {

    /**
     * The refresh mechanism only applies when we can map a resource to a local
     * file
     */
    File sourceFile = resource.getSourceFile();

    if (sourceFile == null)
      return false;

    synchronized (resource) {

      long lastModifiedTime = resource.getLastModifiedTime();

      if (lastModifiedTime >= sourceFile.lastModified())
        return false;

      String existingId = resource.getExternalId();
      _resourceEntriesByExternalId.remove(existingId);

      generateLocalResourceAndExternalUrl(resource);

      return true;
    }
  }

  private void generateLocalResourceAndExternalUrl(ResourceEntry resource) {

    ResourceTransformationStrategy strategy = resource.getTransformationStrategy();

    URL localUrl = resource.getSourceResource();

    if (strategy.requiresTransformation()) {
      File outputFile = getOutputFile(resource.getResourcePath());
      strategy.transformResource(this, localUrl, outputFile);
      localUrl = getFileAsUrl(outputFile);
    }

    resource.setLocalUrl(localUrl);

    String key = getResourceKey(localUrl);

    String externalId = constructExternalId(resource.getResourcePath(), key);
    String externalUrl = constructExternalUrl(externalId);

    resource.setExternalId(externalId);
    resource.setExternalUrl(externalUrl);

    _resourceEntriesByExternalId.put(externalId, resource);
  }

  private String getResourceKey(URL localResource) {
    try {
      InputStream in = localResource.openStream();
      return ResourceSupport.getHash(in);
    } catch (IOException ex) {
      throw new IllegalStateException("error constructing key for resource: "
          + localResource, ex);
    }
  }

  private String constructExternalId(String resourcePath, String resourceKey) {
    String resourceExtension = null;

    int index = resourcePath.lastIndexOf('.');

    if (index != -1) {
      resourceExtension = resourcePath.substring(index + 1);
      resourcePath = resourcePath.substring(0, index);
    }

    StringBuilder b = new StringBuilder();

    b.append(resourcePath);

    if (!_debug) {
      b.append('-');
      b.append(resourceKey);
      b.append(".cache");
    }

    if (resourceExtension != null && resourceExtension.length() > 0)
      b.append('.').append(resourceExtension);

    String url = b.toString();
    return url;
  }

  private String getExternalIdAsResourcePath(String externalId) {

    if (_debug)
      return externalId;

    Matcher m = _resourcePattern.matcher(externalId);

    if (!m.matches())
      return null;

    String resourcePath = m.group(1);

    if (m.groupCount() > 1)
      resourcePath += m.group(2);

    return resourcePath;
  }

  private String constructExternalUrl(String externalId) {

    externalId = externalId.replaceAll("/", "%2f");
    externalId = externalId.replaceAll("\\*", "%2a");
    externalId = externalId.replaceAll(":", "%3a");

    String url = externalId;

    if (_pattern != null)
      url = _pattern.replaceAll("\\{\\}", url);

    if (_prefix != null)
      url = _prefix + url;

    if (_contextPath != null)
      url = _contextPath + url;

    return url;
  }

  /****
   * Multi-Resource Methods
   ****/

  private Resource getResourceForPaths(String resourceId,
      List<String> resourcePaths) {

    if (resourceId == null)
      resourceId = getResourceIdForResourcePaths(resourcePaths);

    Resource resource = _resourceEntriesByResourcePath.get(resourceId);

    if (resource == null) {

      resource = createResourceForPaths(resourceId, resourcePaths);

      if (resource == null)
        return null;

      Resource existingResource = _resourceEntriesByResourcePath.putIfAbsent(
          resourceId, resource);

      if (existingResource != null)
        return existingResource;
    }

    return resource;
  }

  private Resource createResourceForPaths(String resourceId,
      List<String> resourcePaths) {

    List<Resource> resources = new ArrayList<Resource>(resourcePaths.size());

    String extension = null;

    for (String resourcePath : resourcePaths) {

      Resource resource = getResourceForPath(resourcePath, null);

      if (resource == null)
        return null;

      resources.add(resource);

      if (extension == null) {
        int index = resourcePath.lastIndexOf('.');
        if (index != -1)
          extension = resourcePath.substring(index + 1);
      }
    }

    if (extension != null)
      resourceId += "." + extension;

    ResourcesEntry entry = new ResourcesEntry(resourceId, resources);
    generateLocalResourcesAndExternalUrl(entry);
    return entry;
  }

  private String getResourceIdForResourcePaths(List<String> resourcePaths) {

    StringBuilder resourcePathIds = new StringBuilder();

    for (String resourcePath : resourcePaths) {

      if (resourcePathIds.length() > 0)
        resourcePathIds.append(File.pathSeparator);
      resourcePathIds.append(resourcePath);
    }

    return ResourceSupport.getHash(resourcePathIds.toString());
  }

  private boolean refreshResourcesEntry(ResourcesEntry resources) {

    boolean isRefreshed = false;

    synchronized (resources) {

      for (Resource resource : resources.getResources()) {
        if (refreshResource(resource))
          isRefreshed = true;
      }

      if (isRefreshed)
        generateLocalResourcesAndExternalUrl(resources);
    }

    return isRefreshed;
  }

  private void generateLocalResourcesAndExternalUrl(ResourcesEntry entry) {

    File outputFile = getOutputFile(entry.getResourceId());

    try {
      BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));

      for (Resource resource : entry.getResources()) {

        synchronized (resource) {
          URL url = resource.getLocalUrl();
          BufferedReader reader = new BufferedReader(new InputStreamReader(
              url.openStream()));
          String line = null;

          while ((line = reader.readLine()) != null) {
            out.write(line);
            out.write('\n');
          }

          reader.close();
        }
      }

      out.close();
    } catch (IOException ex) {
      throw new IllegalStateException("error constructing resource", ex);
    }

    URL localUrl = getFileAsUrl(outputFile);

    entry.setLocalUrl(localUrl);
    String key = getResourceKey(localUrl);

    String externalId = constructExternalId(entry.getResourceId(), key);
    String externalUrl = constructExternalUrl(externalId);

    entry.setExternalId(externalId);
    entry.setExternalUrl(externalUrl);

    _resourceEntriesByExternalId.put(externalId, entry);
  }

  private String getLocalUrlAsResourceName(URL url) {
    String name = url.toExternalForm();
    int index = name.lastIndexOf('!');
    if (index != -1)
      name = name.substring(index + 1);
    return name;
  }

  private File getOutputFile(String path) {
    path.replace(':', File.separatorChar);
    File file = new File(_tempDir, path);
    File parent = file.getParentFile();
    if (!parent.exists())
      parent.mkdirs();
    return file;
  }

  private URL getFileAsUrl(File outputFile) {
    try {
      URI uri = outputFile.toURI();
      return uri.toURL();
    } catch (MalformedURLException ex) {
      throw new IllegalStateException("couldn't make url from file: "
          + outputFile, ex);
    }
  }
}
