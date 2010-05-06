package org.onebusaway.federations;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public final class FederatedServiceRegistryEntry implements Serializable {

  private static final long serialVersionUID = 1L;

  private String serviceUrl;

  private String serviceClass;

  private Map<String, String> properties;

  private boolean enabled;

  public FederatedServiceRegistryEntry() {

  }

  public FederatedServiceRegistryEntry(String serviceUrl, String serviceClass,
      HashMap<String, String> properties, boolean enabled) {
    this.serviceUrl = serviceUrl;
    this.serviceClass = serviceClass;
    this.properties = properties;
    this.enabled = enabled;
  }

  public String getServiceUrl() {
    return serviceUrl;
  }

  public void setServiceUrl(String serviceUrl) {
    this.serviceUrl = serviceUrl;
  }

  public String getServiceClass() {
    return serviceClass;
  }

  public void setServiceClass(String serviceClass) {
    this.serviceClass = serviceClass;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}
