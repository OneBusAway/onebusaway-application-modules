package org.onebusaway.federations;

import java.util.List;
import java.util.Map;

import org.onebusaway.federations.impl.DynamicFederatedServiceCollectionImpl;
import org.onebusaway.federations.impl.FederatedServiceRegistryImpl;

/**
 * Provides a registry for adding, removing, and querying service class names
 * and their provider locations. While this functionality could be used to
 * manage arbitrary resources, we mostly had in mind managing
 * {@link FederatedService} instances exported as Hessian RPC services and
 * aggregated into one virtual {@link FederatedServiceCollection} using
 * {@link DynamicFederatedServiceCollectionImpl}.
 * 
 * @author bdferris
 * 
 * @see FederatedServiceRegistryEntry
 * @see FederatedService
 * @see FederatedServiceCollection
 * @see DynamicFederatedServiceCollectionImpl
 * @see FederatedServiceRegistryImpl
 */
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
