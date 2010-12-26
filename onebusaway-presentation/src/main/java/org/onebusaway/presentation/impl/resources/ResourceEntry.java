package org.onebusaway.presentation.impl.resources;

import java.io.File;
import java.net.URL;

class ResourceEntry {

  private final String resourcePath;

  private final URL sourceResource;

  private final File sourceFile;

  private final ResourceStrategy resourceStrategy;

  private String externalId;

  private String externalUrl;

  private URL localUrl;

  private long lastModifiedTime;

  public ResourceEntry(String resourcePath, URL sourceResource,
      File sourceFile, ResourceStrategy resourceStrategy) {

    this.resourcePath = resourcePath;
    this.sourceResource = sourceResource;
    this.sourceFile = sourceFile;
    this.resourceStrategy = resourceStrategy;

    if (sourceFile != null)
      lastModifiedTime = sourceFile.lastModified();
    else
      lastModifiedTime = System.currentTimeMillis();
  }

  public String getResourcePath() {
    return resourcePath;
  }

  public File getSourceFile() {
    return sourceFile;
  }

  public URL getSourceResource() {
    return sourceResource;
  }

  public ResourceStrategy getResourceStrategy() {
    return resourceStrategy;
  }

  public String getExternalId() {
    return externalId;
  }

  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }

  public String getExternalUrl() {
    return externalUrl;
  }

  public void setExternalUrl(String externalUrl) {
    this.externalUrl = externalUrl;
  }

  public URL getLocalUrl() {
    return localUrl;
  }

  public void setLocalUrl(URL localUrl) {
    this.localUrl = localUrl;
  }

  public long getLastModifiedTime() {
    return lastModifiedTime;
  }

  public void setLastModifiedTime(long lastModifiedTime) {
    this.lastModifiedTime = lastModifiedTime;
  }
}