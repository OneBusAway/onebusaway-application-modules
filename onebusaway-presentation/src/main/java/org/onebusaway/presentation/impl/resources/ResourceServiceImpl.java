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
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import com.opensymphony.xwork2.StrutsTextProviderFactory;
import org.json.JSONObject;
import org.onebusaway.presentation.impl.ServletLibrary;
import org.onebusaway.presentation.services.resources.Resource;
import org.onebusaway.presentation.services.resources.ResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import com.opensymphony.xwork2.LocaleProvider;
import com.opensymphony.xwork2.TextProvider;
import com.opensymphony.xwork2.TextProviderFactory;

@Component
public class ResourceServiceImpl implements ResourceService {

  public static final String DEBUG_PROPERTY = ResourceServiceImpl.class.getName()
      + ".debug";

  private static final String PREFIX_CLASSPATH = "classpath:";

  private static final String PREFIX_COLLECTION = "collection:";

  private static final String PREFIX_COLLECTION_ENTRY = "collection-entry:";

  private static final String PREFIX_MESSAGES = "messages:";

  private static final String PREFIX_MESSAGES_DATE_LIBRARY = "DateLibrary";

  private static final String PREFIX_FILE = "file:";

  private static final Pattern _resourcePattern = Pattern.compile("^(.*)-\\w+\\.cache(\\.\\w+){0,1}$");

  private static Logger _log = LoggerFactory.getLogger(ResourceServiceImpl.class);

  private static StrutsTextProviderFactory _textProviderFactory = new StrutsTextProviderFactory();

  private ConcurrentMap<String, Resource> _resourceEntriesByResourcePath = new ConcurrentHashMap<String, Resource>();

  private ConcurrentMap<String, Resource> _resourceEntriesByExternalId = new ConcurrentHashMap<String, Resource>();

  private ConcurrentMap<String, List<String>> _resourcePathsById = new ConcurrentHashMap<String, List<String>>();

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
  public String getExternalUrlForResource(String resourcePath, Locale locale) {

    LocaleProvider localeProvider = new LocaleProviderImpl(locale);

    Resource resource = getResourceForPath(resourcePath, localeProvider, null);

    if (resource == null) {
      _log.warn("resource not found: " + resourcePath);
      return null;
    }

    if (_debug)
      refreshResource(resource);

    return resource.getExternalUrl();
  }

  @Override
  public String getExternalUrlForResources(List<String> resourcePaths,
      Locale locale) {

    return getExternalUrlForResources(null, resourcePaths, locale);
  }

  @Override
  public String getExternalUrlForResources(String resourceId,
      List<String> resourcePaths, Locale locale) {

    LocaleProvider localeProvider = new LocaleProviderImpl(locale);

    Resource resource = getResourceForPaths(resourceId, resourcePaths,
        localeProvider);

    if (resource == null) {
      _log.warn("resource not found: " + resourceId);
      return null;
    }

    if (_debug)
      refreshResource(resource);

    return resource.getExternalUrl();
  }

  @Override
  public Resource getLocalResourceForExternalId(String externalId, Locale locale) {

    Resource resource = _resourceEntriesByExternalId.get(externalId);

    if (resource == null) {

      /**
       * In case the resource has not been first requested as a resource(url)
       * first
       */
      String resourcePath = getExternalIdAsResourcePath(externalId);
      if (resourcePath != null) {

        LocaleProvider localeProvider = new LocaleProviderImpl(locale);

        /**
         * First we see if this is a resource identified by id
         */
        if (_resourcePathsById.containsKey(resourcePath)) {
          List<String> paths = _resourcePathsById.get(resourcePath);
          resource = getResourceForPaths(resourcePath, paths, localeProvider);
        }

        if (resource == null)
          resource = getResourceForPath(resourcePath, localeProvider, null);
      }
    }

    if (resource == null) {
      _log.warn("resource not found for external id: " + externalId);
      return null;
    }

    return resource;
  }

  /****
   * Private Methods
   ****/

  private String getResourcePathAsKey(String resourcePath,
      LocaleProvider localeProvider) {

    if (resourcePath.startsWith(PREFIX_MESSAGES)) {
      Locale locale = localeProvider.getLocale();
      return resourcePath + "-" + locale.toString();
    }

    return resourcePath;
  }

  private Resource getResourceForPath(String resourcePath,
      LocaleProvider localeProvider, URL sourceUrl) {

    String resourcePathKey = getResourcePathAsKey(resourcePath, localeProvider);

    Resource resource = _resourceEntriesByResourcePath.get(resourcePathKey);

    if (resource == null) {

      resource = createResourceForPath(resourcePath, sourceUrl, localeProvider);

      if (resource == null)
        return null;

      Resource existingResource = _resourceEntriesByResourcePath.putIfAbsent(
          resourcePathKey, resource);

      if (existingResource != null)
        return existingResource;
    }

    return resource;
  }

  private ResourceEntry createResourceForPath(String resourcePath,
      URL sourceUrl, LocaleProvider localeProvider) {

    if (sourceUrl == null)
      sourceUrl = getResourceAsSourceUrl(resourcePath, localeProvider);

    /**
     * If we can't find a source URL, then we can't create an entry
     */
    if (sourceUrl == null) {
      return null;
    }

    File localFile = getBundleResourceAsLocalFile(resourcePath, sourceUrl);

    ResourceTransformationStrategy strategy = getResourceTransformationStrategyForResource(
        resourcePath, localeProvider);

    ResourceEntry resource = new ResourceEntry(resourcePath, sourceUrl,
        localFile, strategy);
    generateLocalResourceAndExternalUrl(resource);
    return resource;
  }

  private URL getResourceAsSourceUrl(String resourceName,
      LocaleProvider localeProvider) {

    if (resourceName.startsWith(PREFIX_COLLECTION)) {
      resourceName = resourceName.substring(PREFIX_COLLECTION.length());
      return getCollectionResourceAsSourceUrl(resourceName, localeProvider);
    }

    if (resourceName.startsWith(PREFIX_MESSAGES)) {
      resourceName = resourceName.substring(PREFIX_MESSAGES.length());
      if (PREFIX_MESSAGES_DATE_LIBRARY.equals(resourceName)) {
        return getDateLibraryMessagesResourceAsSourceUrl(localeProvider);
      } else {
        return getMessagesResourceAsSourceUrl(resourceName, localeProvider);
      }
    }
    
    if (resourceName.startsWith(PREFIX_CLASSPATH)) {
      resourceName = resourceName.substring(PREFIX_CLASSPATH.length());
      ClassLoader loader = getClass().getClassLoader();
      URL resource = loader.getResource(resourceName);
      if (resource == null)
        _log.warn("unknown classpath resource: name=" + resourceName);
      return resource;
    }

    if (resourceName.startsWith(PREFIX_FILE)) {
      resourceName = resourceName.substring(PREFIX_FILE.length());
      File file = new File(resourceName);
      try {
        return file.toURI().toURL();
      } catch (MalformedURLException e) {
        throw new IllegalStateException("error requesting file url: "
            + resourceName);
      }
    }

    try {
      return _servletContext.getResource(resourceName);
    } catch (MalformedURLException ex) {
      throw new IllegalStateException("error requesting servlet url: "
          + resourceName, ex);
    }

  }

  private URL getCollectionResourceAsSourceUrl(String resourceName,
      LocaleProvider localeProvider) {

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
        Resource r = getResourceForPath(name, localeProvider, url);
        if (r != null) {
          String path = url.getPath();
          int sepIndex = path.lastIndexOf(File.separator);
          if (sepIndex != -1)
            path = path.substring(sepIndex + 1);
          resourceMapping.put(path, r.getExternalUrl());

          String alternateId = PREFIX_COLLECTION_ENTRY + collectionPrefix + ":"
              + path;
          _resourceEntriesByResourcePath.put(alternateId, r);
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

  private URL getMessagesResourceAsSourceUrl(String resourceName,
      LocaleProvider localeProvider) {

    int index = resourceName.indexOf('=');
    if (index == -1)
      throw new IllegalStateException("invalid resource messages specifier: "
          + resourceName);

    String messagesPrefix = resourceName.substring(0, index);
    String messagesResourceClassName = resourceName.substring(index + 1);
    Class<?> messagesResourceClass = null;

    try {
      messagesResourceClass = Class.forName(messagesResourceClassName);
    } catch (Throwable ex) {
      throw new IllegalStateException("error loading messages resource class "
          + messagesResourceClassName, ex);
    }

    TextProvider provider = _textProviderFactory.createInstance(
        messagesResourceClass);
    ResourceBundle bundle = provider.getTexts();

    Map<String, String> resourceMapping = new HashMap<String, String>();

    for (Enumeration<String> en = bundle.getKeys(); en.hasMoreElements();) {
      String key = en.nextElement();
      String value = bundle.getString(key);
      resourceMapping.put(key, value);
    }

    try {

      File file = getOutputFile(PREFIX_MESSAGES + messagesPrefix + ".js");
      PrintWriter out = new PrintWriter(file);
      JSONObject obj = new JSONObject(resourceMapping);
      out.println("var OBA = window.OBA || {};");
      out.println("if(!OBA.Resources) { OBA.Resources = {}; }");
      out.println("OBA.Resources." + messagesPrefix + " = " + obj.toString()
          + ";");
      out.close();

      return getFileAsUrl(file);

    } catch (IOException ex) {
      throw new IllegalStateException("error loading resources", ex);
    }
  }

  private URL getDateLibraryMessagesResourceAsSourceUrl(
      LocaleProvider localeProvider) {

    String messagesPrefix = PREFIX_MESSAGES_DATE_LIBRARY;
    
    DateFormatSymbols symbols = DateFormatSymbols.getInstance(localeProvider.getLocale());

    Map<String, Object> resourceMapping = new HashMap<String, Object>();
    
    resourceMapping.put("amPm", Arrays.asList(symbols.getAmPmStrings()));
    resourceMapping.put("eras", Arrays.asList(symbols.getEras()));
    resourceMapping.put("months", Arrays.asList(symbols.getMonths()));
    resourceMapping.put("shortMonths", Arrays.asList(symbols.getShortMonths()));
    resourceMapping.put("weekdays", Arrays.asList(symbols.getWeekdays()));
    resourceMapping.put("shortWeekdays", Arrays.asList(symbols.getShortWeekdays()));

    try {

      File file = getOutputFile(PREFIX_MESSAGES + messagesPrefix + ".js");
      PrintWriter out = new PrintWriter(file);
      JSONObject obj = new JSONObject(resourceMapping);
      out.println("var OBA = window.OBA || {};");
      out.println("if(!OBA.Resources) { OBA.Resources = {}; }");
      out.println("OBA.Resources." + messagesPrefix + " = " + obj.toString()
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

    if ("jar".equals(protocol)) {

      String path = resourceUrl.getPath();
      int index = path.indexOf('!');
      if (index != -1)
        path = path.substring(0, index);

      try {
        URL jarResourceUrl = new URL(path);
        File file = new File(jarResourceUrl.getPath());
        if (file.exists())
          return file;
      } catch (MalformedURLException e) {

      }
    }

    File path = new File(_servletContext.getRealPath(resourcePath));
    if (path.exists())
      return path;
    return null;
  }

  private ResourceTransformationStrategy getResourceTransformationStrategyForResource(
      String resourcePath, LocaleProvider localeProvider) {
    if (resourcePath.endsWith(".css"))
      return new CssResourceTransformationStrategy(localeProvider.getLocale());
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
    long contentLength = -1;

    if (strategy.requiresTransformation()) {
      File outputFile = getOutputFile(resource.getResourcePath());
      strategy.transformResource(this, localUrl, outputFile);
      localUrl = getFileAsUrl(outputFile);
      contentLength = outputFile.length();
    }

    if (contentLength == -1)
      contentLength = computeContentLengthForLocalUrl(localUrl);

    resource.setLocalUrl(localUrl);
    resource.setContentLength(contentLength);
    String key = getResourceKey(localUrl);

    String externalId = constructExternalId(resource.getResourcePath(), key);
    String externalUrl = constructExternalUrl(externalId);

    resource.setExternalId(externalId);
    resource.setExternalUrl(externalUrl);

    _resourceEntriesByExternalId.put(externalId, resource);
  }

  private long computeContentLengthForLocalUrl(URL localUrl) {

    InputStream in = null;

    try {
      in = localUrl.openStream();
      long contentLength = 0;
      byte[] buffer = new byte[1024];
      while (true) {
        int rc = in.read(buffer);
        if (rc == -1)
          return contentLength;
        contentLength += rc;
      }
    } catch (IOException ex) {
      throw new IllegalStateException("error reading local url " + localUrl, ex);
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException ex) {
          _log.warn("error closing local url " + localUrl, ex);
        }
      }
    }
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
      List<String> resourcePaths, LocaleProvider localeProvider) {

    if (resourceId == null) {
      resourceId = getResourceIdForResourcePaths(resourcePaths);
    } else {
      _resourcePathsById.putIfAbsent(resourceId, resourcePaths);
    }

    String resourceIdKey = getResourcePathAsKey(resourceId, localeProvider);

    Resource resource = _resourceEntriesByResourcePath.get(resourceIdKey);

    if (resource == null) {

      resource = createResourceForPaths(resourceId, resourcePaths,
          localeProvider);

      if (resource == null)
        return null;

      Resource existingResource = _resourceEntriesByResourcePath.putIfAbsent(
          resourceIdKey, resource);

      if (existingResource != null)
        return existingResource;
    }

    return resource;
  }

  private Resource createResourceForPaths(String resourceId,
      List<String> resourcePaths, LocaleProvider localeProvider) {

    List<Resource> resources = new ArrayList<Resource>(resourcePaths.size());

    String extension = null;

    for (String resourcePath : resourcePaths) {

      Resource resource = getResourceForPath(resourcePath, localeProvider, null);

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
    entry.setContentLength(outputFile.length());

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

  private static class LocaleProviderImpl implements LocaleProvider {

    private final Locale _locale;

    public LocaleProviderImpl(Locale locale) {
      _locale = locale;
    }

    @Override
    public Locale getLocale() {
      return _locale;
    }

    @Override
    public boolean isValidLocaleString(String s) {
      if ("en".equalsIgnoreCase(s))
        return true;
      return false;
    }

    @Override
    public boolean isValidLocale(Locale locale) {
      if (locale.getDisplayName().equalsIgnoreCase("en"))
        return true;
      return false;
    }

  }

}
