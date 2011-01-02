package org.onebusaway.presentation.impl.resources;

import java.util.List;

class ResourcesEntry extends AbstractResource {

  private final String resourceId;

  private final List<Resource> resources;

  public ResourcesEntry(String resourceId, List<Resource> resources) {
    this.resourceId = resourceId;
    this.resources = resources;
  }

  public String getResourceId() {
    return resourceId;
  }

  public List<Resource> getResources() {
    return resources;
  }
}