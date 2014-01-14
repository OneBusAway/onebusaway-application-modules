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

import org.onebusaway.federations.EntityIdTestBean;
import org.onebusaway.federations.FederatedServiceCollection;
import org.onebusaway.federations.SimpleFederatedService;
import org.onebusaway.federations.impl.FederatedServiceCollectionImpl;

import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;

public class FederatedByAgencyIdMethodInvocationHandlerImplTest {

  @Test
  public void testSimple() throws Exception {

    SimpleFederatedService mockService = Mockito.mock(SimpleFederatedService.class);
    FederatedServiceCollection mockCollection = Mockito.mock(FederatedServiceCollectionImpl.class);
    Mockito.when(mockCollection.getServiceForAgencyId("agency")).thenReturn(
        mockService);

    Method method = SimpleFederatedService.class.getDeclaredMethod(
        "getValueForId", String.class);
    Object[] args = {"agency_entity"};

    FederatedServiceMethodInvocationHandler handler = new FederatedByEntityIdMethodInvocationHandlerImpl(
        method, 0, "");
    handler.invoke(mockCollection, method, args);

    Mockito.verify(mockService).getValueForId("agency_entity");
  }

  @Test
  public void testArgumentIndex() throws Exception {

    SimpleFederatedService mockService = Mockito.mock(SimpleFederatedService.class);
    FederatedServiceCollection mockCollection = Mockito.mock(FederatedServiceCollectionImpl.class);
    Mockito.when(mockCollection.getServiceForAgencyId("agency")).thenReturn(
        mockService);

    Method method = SimpleFederatedService.class.getDeclaredMethod(
        "getValueForValueAndId", String.class, String.class);
    Object[] args = {"value", "agency_entity"};

    FederatedServiceMethodInvocationHandler handler = new FederatedByEntityIdMethodInvocationHandlerImpl(
        method, 1, "");
    handler.invoke(mockCollection, method, args);

    Mockito.verify(mockService).getValueForValueAndId("value", "agency_entity");
  }

  @Test
  public void testPropertyExpression() throws Exception {

    SimpleFederatedService mockService = Mockito.mock(SimpleFederatedService.class);
    FederatedServiceCollection mockCollection = Mockito.mock(FederatedServiceCollectionImpl.class);
    Mockito.when(mockCollection.getServiceForAgencyId("agency")).thenReturn(
        mockService);

    Method method = SimpleFederatedService.class.getDeclaredMethod(
        "getValueForValueBean", EntityIdTestBean.class);

    EntityIdTestBean value = new EntityIdTestBean("agency_entity");
    Object[] args = {value};

    FederatedServiceMethodInvocationHandler handler = new FederatedByEntityIdMethodInvocationHandlerImpl(
        method, 0, "id");
    handler.invoke(mockCollection, method, args);

    Mockito.verify(mockService).getValueForValueBean(value);
  }
}
