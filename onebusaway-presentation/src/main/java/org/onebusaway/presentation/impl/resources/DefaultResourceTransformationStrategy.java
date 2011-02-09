package org.onebusaway.presentation.impl.resources;

import java.io.File;
import java.net.URL;

import org.onebusaway.presentation.services.resources.ResourceService;

public class DefaultResourceTransformationStrategy implements
    ResourceTransformationStrategy {

  public boolean requiresTransformation() {
    return false;
  }

  @Override
  public void transformResource(ResourceService resourceService,
      URL sourceResource, File targetResource) {

  }
}
