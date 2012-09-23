/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2012 Google, Inc.
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

/**
 * Directs the creation of a {@link FederatedServiceMethodInvocationHandler},
 * where the agency ids that will be used to dispatch the method can potentially
 * be pulled from multiple different argument properties. Agency ids can be
 * pulled from entity ids of the form {@code "agencyId_entityId"}, as identified
 * by {@link #properties()} or from agency ids directly, as identified by
 * {@link #agencyIdProperties()}. Example:
 * 
 * <pre class="code">
 * public class QueryBean {
 *   public String getTripId();
 *   public String getAgencyId();
 * }
 * 
 * @FederatedByAnyEntityIdMethod(argument=0, properties={"tripId"}, agencyIdProperties={"agencyId"})
 * public String getValueForQuery(QueryBean bean);
 * </pre>
 * 
 * This annotation allows for dispatching off so-called query arguments, where
 * differnt arguments may be specified depending on what's being queried.
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

  public String[] agencyIdProperties() default {};
}
