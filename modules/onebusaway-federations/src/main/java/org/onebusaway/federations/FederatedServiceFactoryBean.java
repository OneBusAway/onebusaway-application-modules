package org.onebusaway.federations;

import org.onebusaway.federations.annotations.FederatedServiceMethodInvocationHandler;
import org.onebusaway.federations.annotations.FederatedServiceMethodInvocationHandlerFactory;
import org.onebusaway.geospatial.model.CoordinateBounds;

import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FederatedServiceFactoryBean implements FactoryBean {

  private static FederatedServiceMethodInvocationHandlerFactory _handlerFactory = new FederatedServiceMethodInvocationHandlerFactory();

  private Class<?> _serviceInterface;

  private List<?> _serviceProviders;

  public void setServiceInterface(Class<?> serviceInterface) {
    _serviceInterface = serviceInterface;
  }

  public void setServiceProviders(List<?> serviceProviders) {
    _serviceProviders = serviceProviders;
  }

  /****
   * {@link FactoryBean} Methods
   ****/

  public Class<?> getObjectType() {
    return _serviceInterface;
  }

  public boolean isSingleton() {
    return true;
  }

  public Object getObject() throws Exception {

    if (!_serviceInterface.isInterface())
      throw new IllegalArgumentException("service " + _serviceInterface.getName() + " is not an interface");

    if (!FederatedService.class.isAssignableFrom(_serviceInterface))
      throw new IllegalArgumentException("service interface " + _serviceInterface.getName() + " does not implement "
          + FederatedService.class);
    
    Map<Method,FederatedServiceMethodInvocationHandler> methodHandlers = getMethodHandlers();
    FederatedServiceRegistry registry = getServiceRegistry();
    
    FederatedServiceInvocationHandler handler = new FederatedServiceInvocationHandler(methodHandlers,registry);
    
    Class<?>[] interfaces = { _serviceInterface };
    return Proxy.newProxyInstance(_serviceInterface.getClassLoader(), interfaces, handler);
  }

  private Map<Method, FederatedServiceMethodInvocationHandler> getMethodHandlers() {
    Map<Method, FederatedServiceMethodInvocationHandler> handlers = new HashMap<Method, FederatedServiceMethodInvocationHandler>();
    for (Method method : _serviceInterface.getDeclaredMethods()) {
      FederatedServiceMethodInvocationHandler handler = _handlerFactory.getHandlerForMethod(method);
      handlers.put(method, handler);
    }
    return handlers;
  }

  private FederatedServiceRegistry getServiceRegistry() {

    Map<FederatedService, Map<String, List<CoordinateBounds>>> byProvider = new HashMap<FederatedService, Map<String, List<CoordinateBounds>>>();

    if (_serviceProviders == null || _serviceProviders.isEmpty())
      throw new IllegalArgumentException("no service providers specified for service " + _serviceInterface.getName());

    for (Object serviceProvider : _serviceProviders) {

      if (!_serviceInterface.isAssignableFrom(serviceProvider.getClass()))
        throw new IllegalArgumentException("service provider " + serviceProvider + " not instance of "
            + _serviceInterface.getName());
      FederatedService service = (FederatedService) serviceProvider;
      Map<String, List<CoordinateBounds>> agencyIdsWithCoverageArea = service.getAgencyIdsWithCoverageArea();

      for (Map.Entry<String, List<CoordinateBounds>> entry : agencyIdsWithCoverageArea.entrySet()) {
        String agencyId = entry.getKey();
        List<CoordinateBounds> coverage = entry.getValue();
        checkAgencyAndCoverageAgainstExisting(byProvider, agencyId, coverage);
      }

      byProvider.put(service, agencyIdsWithCoverageArea);
    }

    return new FederatedServiceRegistry(byProvider);
  }

  private void checkAgencyAndCoverageAgainstExisting(
      Map<FederatedService, Map<String, List<CoordinateBounds>>> byProvider, String agencyId,
      List<CoordinateBounds> coverage) {

    for (Map<String, List<CoordinateBounds>> other : byProvider.values()) {

      // Check to see if the specified agencyId has already been defined
      // elsewhere
      if (other.containsKey(agencyId))
        throw new IllegalArgumentException("agency \"" + agencyId + "\" is handled by multiple providers for service "
            + _serviceInterface.getName());

      // Check to see if the agencyId has coverage overlap with an agency from
      // another provider
      for (Map.Entry<String, List<CoordinateBounds>> otherEntry : other.entrySet()) {

        String otherAgencyId = otherEntry.getKey();
        List<CoordinateBounds> otherCoverage = otherEntry.getValue();

        for (CoordinateBounds otherRectangle : otherCoverage) {
          for (CoordinateBounds rectangle : coverage) {
            if (rectangle.intersects(otherRectangle))
              throw new IllegalArgumentException("agency \"" + agencyId + "\" has overlap with agency \""
                  + otherAgencyId + "\" in separate service providers of type " + _serviceInterface.getName());
          }
        }
      }
    }
  }
}
