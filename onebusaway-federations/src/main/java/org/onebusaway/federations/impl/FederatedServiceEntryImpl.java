package org.onebusaway.federations.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.onebusaway.federations.FederatedServiceRegistryEntry;

/**
 * Federated service registry entry helper class for
 * {@link FederatedServiceRegistryImpl}
 * 
 * @author bdferris
 */
public class FederatedServiceEntryImpl {

  private final String _serviceUrl;

  private String _serviceClass;

  private final HashMap<String, String> _properties;

  private boolean _enabled = true;

  private long _expiresAfter = 0;

  public FederatedServiceEntryImpl(String serviceUrl, String serviceClass,
      Map<String, String> properties) {
    _serviceUrl = serviceUrl;
    _serviceClass = serviceClass;
    _properties = new HashMap<String, String>(properties);
  }

  public void setEnabled(boolean enabled) {
    _enabled = enabled;
  }

  /**
   * Determines if a particular service entry is applicable for a set of target
   * properties. A service is applicable if the service has property values
   * equals to all those specified in {@code properties}.
   * 
   * @param properties
   * @return
   */
  public boolean isApplicable(Map<String, String> properties) {

    for (Map.Entry<String, String> entry : properties.entrySet()) {
      String value = _properties.get(entry.getKey());
      if (value == null)
        return false;
      if (!value.equals(entry.getValue()))
        return false;
    }

    return true;
  }

  public long getExpiresAfter() {
    return _expiresAfter;
  }

  public void setExpiresAfter(long expiresAfter) {
    _expiresAfter = expiresAfter;
  }

  public boolean isExpired() {
    return _expiresAfter > 0 && System.currentTimeMillis() > _expiresAfter;
  }

  public String getServiceUrl() {
    return _serviceUrl;
  }

  public String getServiceClass() {
    return _serviceClass;
  }

  public Map<String, String> getProperties() {
    return Collections.unmodifiableMap(_properties);
  }

  public boolean isEnabled() {
    return _enabled;
  }

  public FederatedServiceRegistryEntry getAsEntry() {
    return new FederatedServiceRegistryEntry(_serviceUrl, _serviceClass,
        _properties, _enabled);
  }

}
