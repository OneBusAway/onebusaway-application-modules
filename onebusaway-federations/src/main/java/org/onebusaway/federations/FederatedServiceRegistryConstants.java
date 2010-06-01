package org.onebusaway.federations;

/**
 * Common {@link FederatedServiceRegistry} constants that are typically used to
 * control service registry entry behavior when passed as parameters to
 * {@link FederatedServiceRegistry#addService(String, String, java.util.Map)}
 * and {@link FederatedServiceRegistry#getServices(String, java.util.Map)}
 * 
 * @author bdferris
 */
public class FederatedServiceRegistryConstants {

  /**
   * A service parameter that specifies that a service registry should
   * automatically expire after a specified number of seconds unless the service
   * registry has been republished. Used to automatically expire services if we
   * haven't heard from them in a while.
   */
  public static final String KEY_REGISTRATION_EXPIRES_AFTER = "registration_expires_after";

  /**
   * Controls whether a service registry instance is initial enabled or not.
   * Valid values are "true" or "false".
   */
  public static final String KEY_INITIALLY_ENABLED = "initially_enabled";

}
