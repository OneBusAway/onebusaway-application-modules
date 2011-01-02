package org.onebusaway.presentation.impl.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.onebusaway.presentation.services.resources.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;

public class ResourceFactory {

  private ResourceService _resourceService;

  private String _name;

  private List<String> _resources = new ArrayList<String>();

  @Autowired
  public void setResourceService(ResourceService resourceService) {
    _resourceService = resourceService;
  }

  public void setName(String name) {
    _name = name;
  }

  public void setResource(String resource) {
    _resources = Arrays.asList(resource);
  }

  public void setResources(List<String> resources) {
    _resources = resources;
  }

  @PostConstruct
  public void setup() {
    _resourceService.getExternalUrlForResources(_name, _resources);
  }
}
