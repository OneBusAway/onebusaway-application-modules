package org.onebusaway.presentation.impl.resources;

import java.io.File;
import java.net.URL;

import org.onebusaway.presentation.services.resources.ResourceService;

public interface ResourceTransformationStrategy {
  
  public boolean requiresTransformation();

  public void transformResource(ResourceService resourceService, URL sourceResource, File targetResource);
}
