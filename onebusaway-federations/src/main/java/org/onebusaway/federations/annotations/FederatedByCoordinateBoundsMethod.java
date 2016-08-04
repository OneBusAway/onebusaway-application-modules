/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.federations.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.onebusaway.collections.beans.PropertyPathExpression;
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
