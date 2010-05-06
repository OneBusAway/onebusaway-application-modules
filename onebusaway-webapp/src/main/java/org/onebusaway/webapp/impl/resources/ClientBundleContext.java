package org.onebusaway.webapp.impl.resources;

import java.io.File;


public interface ClientBundleContext {

  public String handleResource(String bundleName, String resourceName,
      String resourceKey, String resourceExtension, LocalResource resource);

  public String addContext(String url);

  public File getTempDir();
}
