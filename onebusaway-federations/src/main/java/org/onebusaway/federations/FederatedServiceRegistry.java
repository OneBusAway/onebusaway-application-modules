package org.onebusaway.federations;

import java.util.List;
import java.util.Map;

public interface FederatedServiceRegistry {

  public void addService(String url, String serviceClass,
      Map<String, String> properties);

  public void removeService(String url);

  public List<FederatedServiceRegistryEntry> getServices(String serviceClass,
      Map<String, String> properties);

  public void setServiceStatus(String url, boolean enabled);

  public List<FederatedServiceRegistryEntry> getAllServices();

  public void removeAllServices();
}
