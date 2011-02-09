package org.onebusaway.federations.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.onebusaway.federations.FederatedService;

/**
 * Directs the creation of a {@link FederatedServiceMethodInvocationHandler},
 * where two double arguments define a lat-lon point, resolving an
 * appropriate {@link FederatedService} handler for that location.
 * 
 * <pre class="code">
 * @FederatedByLocationMethod
 * public String getValueForLocation(double lat, double lon);
 * </pre>
 * 
 * @author bdferris
 * @see FederatedServiceMethodInvocationHandlerFactory
 * @see FederatedByLocationMethodInvocationHandlerImpl
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface FederatedByLocationMethod {
  public int latArgument() default 0;

  public int lonArgument() default 1;
}
