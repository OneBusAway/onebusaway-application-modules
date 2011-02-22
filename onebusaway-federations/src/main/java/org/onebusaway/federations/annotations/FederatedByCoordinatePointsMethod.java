package org.onebusaway.federations.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.onebusaway.collections.PropertyPathExpression;
import org.onebusaway.federations.FederatedService;
import org.onebusaway.geospatial.model.CoordinatePoint;

/**
 * Directs the creation of a {@link FederatedServiceMethodInvocationHandler},
 * where a series of  {@link CoordinatePoint} objects describing a geographic area
 * are resolved to an appropriate {@link FederatedService} handler for that area.
 * 
 * <pre class="code">
 * @FederatedByCoordinatePointsMethod
 * public String getValueForCoordinatePoint(CoordinatePoint point);
 * 
 * @FederatedByCoordinatePointsMethod(propertyExpression="point")
 * public String getValueForCoordinatePointsTestBean(CoordinatePointsTestBean bounds);
 * 
 * @FederatedByCoordinatePointsMethod(arguments={0,1})
 * public String getValueForCoordinatePoints(CoordinatePoint a, CoordinatePoint b);
 * </pre>
 * 
 * In addition to specifying the position of the bounds argument with
 * {@link #arguments()}, an optional {@link #propertyExpressions()} allows you to
 * specify a {@link PropertyPathExpression} that can be used to resolve a
 * {@link CoordinatePoint} object that is a property of a bean that is an
 * argument in the federated method invocation.
 * 
 * @author bdferris
 * @see FederatedServiceMethodInvocationHandlerFactory
 * @see FederatedByCoordinatePointsMethodInvocationHandlerImpl
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface FederatedByCoordinatePointsMethod {
  public int[] arguments() default 0;

  public String[] propertyExpressions() default {};
}
