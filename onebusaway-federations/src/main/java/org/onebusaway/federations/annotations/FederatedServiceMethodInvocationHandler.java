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

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.federations.FederatedService;
import org.onebusaway.federations.FederatedServiceCollection;
import org.onebusaway.federations.FederatedServiceFactoryBean;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Defines a {@link FederatedService} method invocation handler. The handler is
 * responsible for examining the arguments of the method invocation to determine
 * an appropriate {@link FederatedService} from the passed
 * {@link FederatedServiceCollection} for handling the method.
 * 
 * The {@link FederatedServiceMethodInvocationHandlerFactory} can be used to
 * examine a Method signature and create an appropriate
 * {@link FederatedServiceMethodInvocationHandler} based on method annotations.
 * 
 * @author bdferris
 * @see FederatedServiceMethodInvocationHandlerFactory
 * @see FederatedServiceFactoryBean
 */
public interface FederatedServiceMethodInvocationHandler {

  public Object invoke(FederatedServiceCollection collection, Method method,
      Object[] args) throws ServiceException, IllegalArgumentException,
      IllegalAccessException, InvocationTargetException;
}
