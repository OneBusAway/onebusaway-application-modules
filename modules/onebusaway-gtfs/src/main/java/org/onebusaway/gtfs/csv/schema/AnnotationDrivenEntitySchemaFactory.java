package org.onebusaway.gtfs.csv.schema;

import org.onebusaway.gtfs.csv.schema.annotations.CsvFields;
import org.onebusaway.gtfs.csv.schema.beans.CsvEntityMappingBean;
import org.onebusaway.gtfs.csv.schema.beans.CsvFieldMappingBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class AnnotationDrivenEntitySchemaFactory extends AbstractEntitySchemaFactoryImpl {

  /** URL prefix for loading from the file system: "file:" */
  private static final String FILE_URL_PREFIX = "file:";

  /** URL protocol for a file in the file system: "file" */
  private static final String URL_PROTOCOL_FILE = "file";

  /** URL protocol for an entry from a jar file: "jar" */
  private static final String URL_PROTOCOL_JAR = "jar";

  /** URL protocol for an entry from a zip file: "zip" */
  private static final String URL_PROTOCOL_ZIP = "zip";

  /** URL protocol for an entry from a WebSphere jar file: "wsjar" */
  private static final String URL_PROTOCOL_WSJAR = "wsjar";

  /** URL protocol for an entry from an OC4J jar file: "code-source" */
  private static final String URL_PROTOCOL_CODE_SOURCE = "code-source";

  /** Separator between JAR URL and file path within the JAR */
  private static final String JAR_URL_SEPARATOR = "!/";

  private final Logger _log = LoggerFactory.getLogger(AnnotationDrivenEntitySchemaFactory.class);

  private List<String> _packagesToScan = new ArrayList<String>();

  private List<Class<?>> _classesToScan = new ArrayList<Class<?>>();

  public void addPackageToScan(String packageToScan) {
    _packagesToScan.add(packageToScan);
  }

  public void addEntityClass(Class<?> classToScan) {
    _classesToScan.add(classToScan);
  }

  @Override
  protected void processBeanDefinitions() {

    for (Class<?> entityClass : _classesToScan) {
      CsvEntityMappingBean bean = getEntityMappingBeanForEntityClass(entityClass);
      registerBeanDefinition(bean);
    }

    try {
      scanPackages();
    } catch (IOException ex) {
      _log.warn("error scanning classpath for classes", ex);
    }
  }

  /****
   * Private Methods
   ****/

  private void go(String cName) {
    try {
      Class<?> entityClass = Class.forName(cName);
      CsvFields csvFields = entityClass.getAnnotation(CsvFields.class);
      if (csvFields != null) {
        CsvEntityMappingBean mappingBean = getEntityMappingBeanForEntityClass(entityClass);
        registerBeanDefinition(mappingBean);
      }
    } catch (ClassNotFoundException ex) {

    }
  }

  private CsvEntityMappingBean getEntityMappingBeanForEntityClass(Class<?> entityClass) {

    CsvFields csvFields = entityClass.getAnnotation(CsvFields.class);

    if (csvFields == null)
      throw new IllegalStateException("no csv fields info for entity class: " + entityClass);

    CsvEntityMappingBean bean = new CsvEntityMappingBean(entityClass);
    applyCsvFieldsAnnotationToBean(entityClass, bean);

    for (Field field : entityClass.getDeclaredFields()) {

      // Skip static final fields
      if ((field.getModifiers() & (Modifier.FINAL | Modifier.STATIC)) != 0)
        continue;

      CsvFieldMappingBean fieldBean = new CsvFieldMappingBean(field);
      applyCsvFieldAnnotationToBean(field, fieldBean);

      bean.addField(fieldBean);
    }
    return bean;
  }

  private void scanPackages() throws IOException {

    ClassLoader cl = AnnotationDrivenEntitySchemaFactory.class.getClassLoader();

    for (String packageToScan : _packagesToScan) {

      if (packageToScan != null) {

        String pkg = packageToScan.replace('.', '/');

        for (Enumeration<URL> en = cl.getResources(pkg); en.hasMoreElements();) {
          URL url = en.nextElement();

          if (isJarURL(url)) {

            URL jarURL = extractJarFileURL(url);
            File jarPath = getFile(jarURL);
            JarFile jar = new JarFile(jarPath);

            for (Enumeration<JarEntry> en2 = jar.entries(); en2.hasMoreElements();) {
              JarEntry entry = en2.nextElement();

              String name = entry.getName();
              if (name.startsWith(pkg) && name.endsWith(".class")) {
                String cName = name.replace(".class", "").replace('/', '.');
                go(cName);
              }
            }
          } else {
            String path = URLDecoder.decode(url.getPath(), "UTF-8");

            String root = new File(path.replace(pkg, "")).getAbsolutePath();
            if (!root.endsWith(File.separator))
              root += File.separator;

            scanFile(root, new File(path));
          }
        }
      }
    }
  }

  private void scanFile(String root, File f) {
    if (f.isDirectory()) {
      File[] files = f.listFiles();
      if (files != null) {
        for (File fChild : files)
          scanFile(root, fChild);
      }
    } else if (f.getName().endsWith(".class")) {
      String cName = f.getAbsolutePath().replace(root, "").replace(".class", "").replace('/', '.');
      go(cName);
    }
  }

  /***************************************************************************
   * Classpath URL Wrangling Methods as pulled from
   * 
   * org.springframework.util.ResourceUtils
   * 
   * in the Spring Framework
   * 
   * Copyright 2002-2008 the original author or authors.
   * 
   * Licensed under the Apache License, Version 2.0 (the "License"); you may not
   * use this file except in compliance with the License. You may obtain a copy
   * of the License at
   * 
   * http://www.apache.org/licenses/LICENSE-2.0
   * 
   * Unless required by applicable law or agreed to in writing, software
   * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
   * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
   * License for the specific language governing permissions and limitations
   * under the License.
   **************************************************************************/

  /**
   * Determine whether the given URL points to a resource in a jar file, that
   * is, has protocol "jar", "zip", "wsjar" or "code-source".
   * <p>
   * "zip" and "wsjar" are used by BEA WebLogic Server and IBM WebSphere,
   * respectively, but can be treated like jar files. The same applies to
   * "code-source" URLs on Oracle OC4J, provided that the path contains a jar
   * separator.
   * 
   * @param url the URL to check
   * @return whether the URL has been identified as a JAR URL
   */
  private static boolean isJarURL(URL url) {
    String protocol = url.getProtocol();
    return (URL_PROTOCOL_JAR.equals(protocol) || URL_PROTOCOL_ZIP.equals(protocol)
        || URL_PROTOCOL_WSJAR.equals(protocol) || (URL_PROTOCOL_CODE_SOURCE.equals(protocol) && url.getPath().indexOf(
        JAR_URL_SEPARATOR) != -1));
  }

  /**
   * Extract the URL for the actual jar file from the given URL (which may point
   * to a resource in a jar file or to a jar file itself).
   * 
   * @param jarUrl the original URL
   * @return the URL for the actual jar file
   * @throws MalformedURLException if no valid jar file URL could be extracted
   */
  private static URL extractJarFileURL(URL jarUrl) throws MalformedURLException {
    String urlFile = jarUrl.getFile();
    int separatorIndex = urlFile.indexOf(JAR_URL_SEPARATOR);
    if (separatorIndex != -1) {
      String jarFile = urlFile.substring(0, separatorIndex);
      try {
        return new URL(jarFile);
      } catch (MalformedURLException ex) {
        // Probably no protocol in original jar URL, like
        // "jar:C:/mypath/myjar.jar".
        // This usually indicates that the jar file resides in the file
        // system.
        if (!jarFile.startsWith("/")) {
          jarFile = "/" + jarFile;
        }
        return new URL(FILE_URL_PREFIX + jarFile);
      }
    } else {
      return jarUrl;
    }
  }

  private static File getFile(URL resourceUrl) throws FileNotFoundException {
    if (!URL_PROTOCOL_FILE.equals(resourceUrl.getProtocol())) {
      throw new FileNotFoundException("url cannot be resolved to absolute file path "
          + "because it does not reside in the file system: " + resourceUrl);
    }
    try {
      return new File(toURI(resourceUrl).getSchemeSpecificPart());
    } catch (URISyntaxException ex) {
      // Fallback for URLs that are not valid URIs (should hardly ever
      // happen).
      return new File(resourceUrl.getFile());
    }
  }

  /**
   * Create a URI instance for the given URL, replacing spaces with "%20" quotes
   * first.
   * <p>
   * Furthermore, this method works on JDK 1.4 as well, in contrast to the
   * <code>URL.toURI()</code> method.
   * 
   * @param url the URL to convert into a URI instance
   * @return the URI instance
   * @throws URISyntaxException if the URL wasn't a valid URI
   * @see java.net.URL#toURI()
   */
  private static URI toURI(URL url) throws URISyntaxException {
    return toURI(url.toString());
  }

  /**
   * Create a URI instance for the given location String, replacing spaces with
   * "%20" quotes first.
   * 
   * @param location the location String to convert into a URI instance
   * @return the URI instance
   * @throws URISyntaxException if the location wasn't a valid URI
   */
  private static URI toURI(String location) throws URISyntaxException {
    return new URI(location.replaceAll(" ", "%20"));
  }

}
