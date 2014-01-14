/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
