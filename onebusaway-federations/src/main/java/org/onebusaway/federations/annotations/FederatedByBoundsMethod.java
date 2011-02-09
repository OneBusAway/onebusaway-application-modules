package org.onebusaway.federations.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.onebusaway.federations.FederatedService;

/**
 * Directs the creation of a {@link FederatedServiceMethodInvocationHandler},
 * where four double arguments define two lat-lon points describing the bounds
 * of a geographic area, resolving an appropriate {@link FederatedService}
 * handler for that area.
 * 
 * <pre class="code">
 * @FederatedByBoundsMethod
 * public String getValueForBounds(double lat1, double lon1, double lat2, double lon2);
 * </pre>
 * 
 * @author bdferris
 * @see FederatedServiceMethodInvocationHandlerFactory
 * @see FederatedByCoordinateBoundsMethodInvocationHandlerImpl
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface FederatedByBoundsMethod {
  public int lat1Argument() default 0;

  public int lon1Argument() default 1;

  public int lat2Argument() default 2;

  public int lon2Argument() default 3;
}
