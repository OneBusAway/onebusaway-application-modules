package org.onebusaway.presentation.impl.resources;

import java.io.File;
import java.net.URL;

class ResourceEntry extends AbstractResource {

  private final String resourcePath;

  private final URL sourceResource;

  private final File sourceFile;

  private final ResourceTransformationStrategy transformationStrategy;

  public ResourceEntry(String resourcePath, URL sourceResource,
      File sourceFile, ResourceTransformationStrategy transformationStrategy) {

    this.resourcePath = resourcePath;
    this.sourceResource = sourceResource;
    this.sourceFile = sourceFile;
    this.transformationStrategy = transformationStrategy;

    if (sourceFile != null)
      setLastModifiedTime(sourceFile.lastModified());
    else
      setLastModifiedTime(System.currentTimeMillis());
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

  public ResourceTransformationStrategy getTransformationStrategy() {
    return transformationStrategy;
  }
}