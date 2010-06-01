package org.onebusaway.federations.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.onebusaway.collections.PropertyPathExpression;
import org.onebusaway.federations.FederatedService;
import org.onebusaway.geospatial.model.CoordinateBounds;

/**
 * Directs the creation of a {@link FederatedServiceMethodInvocationHandler},
 * where a single {@link CoordinateBounds} object describing a geographic area
 * is resolved to an appropriate {@link FederatedService} handler for that area.
 * 
 * <pre class="code">
 * @FederatedByCoordinateBoundsMethod
 * public String getValueForCoordinateBounds(CoordinateBounds bounds);
 * 
 * @FederatedByCoordinateBoundsMethod(propertyExpression="bounds")
 * public String getValueForCoordinateBoundsTestBean(CoordinateBoundsTestBean bounds);
 * </pre>
 * 
 * In addition to specifying the position of the bounds argument with
 * {@link #argument()}, an optional {@link #propertyExpression()} allows you to
 * specify an {@link PropertyPathExpression} that can be used to resolve a
 * {@link CoordinateBounds} object that is a property of a bean that is an
 * argument in the federated method invocation.
 * 
 * @author bdferris
 * @see FederatedServiceMethodInvocationHandlerFactory
 * @see FederatedByCoordinateBoundsMethodInvocationHandlerImpl
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface FederatedByCoordinateBoundsMethod {
  public int argument() default 0;

  public String propertyExpression() default "";
}
