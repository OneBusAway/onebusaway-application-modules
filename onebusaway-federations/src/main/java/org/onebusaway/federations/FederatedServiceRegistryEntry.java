package org.onebusaway.federations;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Information about a {@link FederatedServiceRegistry} entry. This is just a
 * static Java bean. Editing values in an entry returned by a
 * {@link FederatedServiceRegistry} will not update values in the registry
 * itself.
 * 
 * @author bdferris
 */
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

  /**
   * @return the resource locator url for the service entry
   */
  public String getServiceUrl() {
    return serviceUrl;
  }

  public void setServiceUrl(String serviceUrl) {
    this.serviceUrl = serviceUrl;
  }

  /**
   * @return the service class type for the service entry
   */
  public String getServiceClass() {
    return serviceClass;
  }

  public void setServiceClass(String serviceClass) {
    this.serviceClass = serviceClass;
  }

  /**
   * @return properties for the service entry
   */
  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  /**
   * @return true if the service entry is enabled, otherwise false
   */
  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}
