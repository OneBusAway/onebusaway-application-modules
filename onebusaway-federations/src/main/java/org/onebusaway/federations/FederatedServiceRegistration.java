package org.onebusaway.federations;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Periodically publishes service registration information to a
 * {@link FederatedServiceRegistry} on a fixed interval. In combination with
 * {@link FederatedServiceRegistryConstants#KEY_REGISTRATION_EXPIRES_AFTER}, we
 * can implement some basic fail-over capabilities in the service registry such
 * that a registry entry is automatically removed after a certain period of time
 * if the service hasn't been heard from in a while (ex. the service has
 * crashed).
 * 
 * @author bdferris
 * @see FederatedServiceRegistryConstants#KEY_REGISTRATION_EXPIRES_AFTER
 */
public class FederatedServiceRegistration {

  private static Logger _log = LoggerFactory.getLogger(FederatedServiceRegistration.class);

  private FederatedServiceRegistry _registry;

  private String _serviceUrl;

  private String _serviceClass;

  private Map<String, String> _properties = new HashMap<String, String>();

  private boolean _initiallyEnabled = true;

  private int _updateFrequency = 60;

  private ScheduledExecutorService _executor = Executors.newSingleThreadScheduledExecutor();

  public void setRegistry(FederatedServiceRegistry registry) {
    _registry = registry;
  }

  public void setServiceUrl(String url) {
    _serviceUrl = url;
  }

  public void setServiceClass(Class<?> serviceClass) {
    _serviceClass = serviceClass.getName();
  }

  public void setProperties(Map<String, String> properties) {
    _properties = properties;
  }

  public void setUpdateFrequency(int updateFrequencyInSeconds) {
    _updateFrequency = updateFrequencyInSeconds;
  }

  public void setInitiallyEnabled(boolean initiallyEnabled) {
    _initiallyEnabled = initiallyEnabled;
  }

  public void start() {
    _executor.scheduleAtFixedRate(new ServiceUpdateImpl(), 0, _updateFrequency,
        TimeUnit.SECONDS);
  }

  public void stop() {
    _executor.shutdown();
    try {
      _registry.removeService(_serviceUrl);
    } catch (Throwable ex) {
      _log.warn("error removing service registration", ex);
    }
  }

  /****
   * Private Methods
   ****/

  private class ServiceUpdateImpl implements Runnable {
    @Override
    public void run() {
      try {

        Map<String, String> properties = new HashMap<String, String>();
        properties.put(
            FederatedServiceRegistryConstants.KEY_REGISTRATION_EXPIRES_AFTER,
            Integer.toString(_updateFrequency * 2));
        properties.put(FederatedServiceRegistryConstants.KEY_INITIALLY_ENABLED,
            Boolean.toString(_initiallyEnabled));
        properties.putAll(_properties);

        _registry.addService(_serviceUrl, _serviceClass, properties);
      } catch (Throwable ex) {
        _log.warn("error adding service registration", ex);
      }
    }
  }
}
