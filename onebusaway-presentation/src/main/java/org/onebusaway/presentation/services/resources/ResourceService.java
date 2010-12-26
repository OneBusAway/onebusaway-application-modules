package org.onebusaway.presentation.services.resources;

import java.net.URL;

public interface ResourceService {
  
  public String getExternalUrlForResource(String resourcePath);

  public URL getLocalUrlForExternalId(String externalId);
}
