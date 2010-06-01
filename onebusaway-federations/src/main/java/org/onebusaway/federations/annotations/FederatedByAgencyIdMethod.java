package org.onebusaway.federations.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.onebusaway.federations.FederatedService;

/**
 * Directs the creation of a {@link FederatedServiceMethodInvocationHandler},
 * where a single String argument in the method invocation is used as an agency
 * id in resolving an appropriate {@link FederatedService} handler.
 * 
 * <pre class="code">
 * @FederatedByAgencyIdMethod
 * public String getAgencyName(String agencyId);
 * </pre>
 * 
 * @author bdferris
 * @see FederatedServiceMethodInvocationHandlerFactory
 * @see FederatedByAgencyIdMethodInvocationHandlerImpl
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface FederatedByAgencyIdMethod {
  /**
   * @return the index of the method argument containing the agency id
   */
  public int argument() default 0;
}
