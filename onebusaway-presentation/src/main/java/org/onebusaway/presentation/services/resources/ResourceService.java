package org.onebusaway.presentation.services.resources;

import java.util.List;
import java.util.Locale;

public interface ResourceService {

  public String getExternalUrlForResource(String resourcePath, Locale locale);

  public String getExternalUrlForResources(List<String> resourcePaths, Locale locale);
  
  public String getExternalUrlForResources(String resourceId, List<String> resourcePaths, Locale locale);

  public Resource getLocalResourceForExternalId(String externalId, Locale locale);
}
