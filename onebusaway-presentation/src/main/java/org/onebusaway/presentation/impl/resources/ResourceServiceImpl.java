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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import org.onebusaway.presentation.impl.ServletLibrary;
import org.onebusaway.presentation.services.resources.ResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ResourceServiceImpl implements ResourceService {

  public static final String DEBUG_PROPERTY = ResourceServiceImpl.class.getName()
      + ".debug";

  private static final String PREFIX_CLASSPATH = "classpath:";

  private static final Pattern _resourcePattern = Pattern.compile("^(.*)-\\w+\\.cache(\\.\\w+){0,1}$");
  
  private static Logger _log = LoggerFactory.getLogger(ResourceServiceImpl.class);

  private ConcurrentMap<String, ResourceEntry> _resourceEntriesByResourcePath = new ConcurrentHashMap<String, ResourceEntry>();

  private ConcurrentMap<String, ResourceEntry> _resourceEntriesByExternalId = new ConcurrentHashMap<String, ResourceEntry>();

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
    String value = System.getProperty(DEBUG_PROPERTY,"false");
    if( value != null && value.toLowerCase().equals("true"))
      _debug = true;
  }

  /****
   * {@link ResourceService} Interface
   ****/
  
  @Override
  public String getExternalUrlForResource(String resourcePath) {

    ResourceEntry resource = getResourceForPath(resourcePath);

    if (resource == null)
      return null;

    refreshResource(resource);

    return resource.getExternalUrl();
  }

  @Override
  public URL getLocalUrlForExternalId(String externalId) {
    ResourceEntry resource = _resourceEntriesByExternalId.get(externalId);
    if (resource == null) {
      /**
       * In case the resource has not been first requested as a resource(url)
       * first
       */
      String resourcePath = getExternalIdAsResourcePath(externalId);
      if (resourcePath != null)
        resource = getResourceForPath(resourcePath);
    }
    if (resource == null)
      return null;
    return resource.getLocalUrl();
  }

  /****
   * Private Methods
   ****/

  private ResourceEntry getResourceForPath(String resourcePath) {

    ResourceEntry resource = _resourceEntriesByResourcePath.get(resourcePath);

    if (resource == null) {

      resource = createResourceForPath(resourcePath);

      if (resource == null)
        return null;

      ResourceEntry existingResource = _resourceEntriesByResourcePath.putIfAbsent(
          resourcePath, resource);

      if (existingResource != null)
        return existingResource;
    }

    return resource;
  }

  private ResourceEntry createResourceForPath(String resourcePath) {

    URL sourceUrl = getResourceAsSourceUrl(resourcePath);

    /**
     * If we can't find a source URL, then we can't create an entry
     */
    if (sourceUrl == null)
      return null;

    File localFile = getBundleResourceAsLocalFile(resourcePath, sourceUrl);

    ResourceStrategy strategy = getResourceStrategyForResource(resourcePath);

    ResourceEntry resource = new ResourceEntry(resourcePath, sourceUrl,
        localFile, strategy);
    generateLocalResourceAndExternalUrl(resource);
    return resource;
  }

  private URL getResourceAsSourceUrl(String resourceName) {

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

  private File getBundleResourceAsLocalFile(String resourcePath, URL resourceUrl) {
    String protocol = resourceUrl.getProtocol();
    if ("file".equals(protocol))
      return new File(resourceUrl.getPath());
    File path = new File(_servletContext.getRealPath(resourcePath));
    if (path.exists())
      return path;
    return null;
  }

  private ResourceStrategy getResourceStrategyForResource(String resourcePath) {
    return new DefaultResourceStrategyImpl();
  }

  private void refreshResource(ResourceEntry resource) {

    /**
     * The refresh mechanism only applies when we can map a resource to a local
     * file
     */
    File sourceFile = resource.getSourceFile();

    if (sourceFile == null)
      return;

    synchronized (resource) {

      long lastModifiedTime = resource.getLastModifiedTime();

      if (lastModifiedTime >= sourceFile.lastModified())
        return;

      String existingId = resource.getExternalId();
      _resourceEntriesByExternalId.remove(existingId);

      generateLocalResourceAndExternalUrl(resource);
    }
  }

  private void generateLocalResourceAndExternalUrl(ResourceEntry resource) {

    ResourceStrategy strategy = resource.getResourceStrategy();

    URL localUrl = strategy.getSourceResourceAsLocalResource(resource.getSourceResource());
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

    resourcePath = resourcePath.replaceAll("/", "%2f");

    b.append(resourcePath);
    
    if( ! _debug ) {
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
    
    if( _debug )
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

    String url = externalId;

    if (_pattern != null)
      url = _pattern.replaceAll("\\{\\}", url);

    if (_prefix != null)
      url = _prefix + url;

    if (_contextPath != null)
      url = _contextPath + url;

    return url;
  }

}
