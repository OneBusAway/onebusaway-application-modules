package org.onebusaway.federations.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.onebusaway.federations.FederatedService;

/**
 * Directs the creation of a {@link FederatedServiceMethodInvocationHandler},
 * where a Collection of String ids of the form {@code "agencyId_entityId"} are
 * resolved to a set of agency ids and an appropriate {@link FederatedService}
 * handler for those agencies.
 * 
 * <pre class="code">
 * @FederatedByEntityIdsMethod
 * public String getValueForIds(Set<String> entityId);
 * </pre>
 * 
 * @author bdferris
 * @see FederatedServiceMethodInvocationHandlerFactory
 * @see FederatedByEntityIdsMethodInvocationHandlerImpl
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface FederatedByAnyEntityIdMethod {
  public int argument() default 0;
  
  public String[] properties() default {};
}
