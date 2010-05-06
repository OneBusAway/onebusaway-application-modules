package org.onebusaway.gtfs_diff.impl;

import org.onebusaway.gtfs_diff.services.GtfsDifferenceService;

import java.util.List;

public class GtfsDifferenceServiceImpl implements GtfsDifferenceService {

  private List<GtfsDifferenceService> _services;

  public void setServices(List<GtfsDifferenceService> services) {
    _services = services;
  }

  public void computeDifferences() {
    for (GtfsDifferenceService service : _services)
      service.computeDifferences();
  }
}
