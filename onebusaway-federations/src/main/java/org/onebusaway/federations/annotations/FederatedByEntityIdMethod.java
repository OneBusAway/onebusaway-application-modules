package org.onebusaway.federations.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.onebusaway.collections.PropertyPathExpression;
import org.onebusaway.federations.FederatedService;

/**
 * Directs the creation of a {@link FederatedServiceMethodInvocationHandler},
 * where a single String id of the form {@code "agencyId_entityId"} is resolved
 * to an agency id and an appropriate {@link FederatedService} handler for that
 * agency.
 * 
 * <pre class="code">
 * @FederatedByEntityIdMethod
 * public String getValueForId(String entityId);
 * 
 * @FederatedByEntityIdMethod(propertyExpression="id")
 * public String getValueForValueBean(EntityIdTestBean value);
 * </pre>
 * 
 * In addition to specifying the position of the id argument with
 * {@link #argument()}, an optional {@link #propertyExpression()} allows you to
 * specify an {@link PropertyPathExpression} that can be used to resolve a id
 * that is a property of a bean that is an argument in the federated method
 * invocation.
 * 
 * @author bdferris
 * @see FederatedServiceMethodInvocationHandlerFactory
 * @see FederatedByEntityIdMethod
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface FederatedByEntityIdMethod {
  public int argument() default 0;

  public String propertyExpression() default "";
}
