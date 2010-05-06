/**
 * 
 */
package org.onebusaway.metrokc2gtfs.model;


import java.util.Set;

public class ServiceId {

  private final String _serviceId;
  private final Set<ServiceIdModification> _mods;

  public ServiceId(String serviceId, Set<ServiceIdModification> mods) {
    _serviceId = serviceId;
    _mods = mods;
  }

  public String getServiceId() {
    return _serviceId;
  }

  public Set<ServiceIdModification> getModifications() {
    return _mods;
  }
}