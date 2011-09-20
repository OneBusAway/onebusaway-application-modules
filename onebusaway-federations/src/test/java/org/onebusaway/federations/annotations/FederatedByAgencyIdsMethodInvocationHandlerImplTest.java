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

import org.onebusaway.federations.FederatedServiceCollection;
import org.onebusaway.federations.SimpleFederatedService;
import org.onebusaway.federations.impl.FederatedServiceCollectionImpl;

import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class FederatedByAgencyIdsMethodInvocationHandlerImplTest {

  @Test
  public void testSimple() throws Exception {

    Set<String> agencyIds = new HashSet<String>();
    agencyIds.add("agency");

    Set<String> ids = new HashSet<String>();
    ids.add("agency_entity");

    SimpleFederatedService mockService = Mockito.mock(SimpleFederatedService.class);
    FederatedServiceCollection mockCollection = Mockito.mock(FederatedServiceCollectionImpl.class);
    Mockito.when(mockCollection.getServiceForAgencyIds(agencyIds)).thenReturn(mockService);

    Method method = SimpleFederatedService.class.getDeclaredMethod("getValueForIds", Set.class);
    Object[] args = {ids};

    FederatedServiceMethodInvocationHandler handler = new FederatedByEntityIdsMethodInvocationHandlerImpl(0);
    handler.invoke(mockCollection, method, args);

    Mockito.verify(mockService).getValueForIds(ids);
  }
  
  @Test
  public void testArgumentIndex() throws Exception {

    Set<String> agencyIds = new HashSet<String>();
    agencyIds.add("agency");

    Set<String> ids = new HashSet<String>();
    ids.add("agency_entity");

    SimpleFederatedService mockService = Mockito.mock(SimpleFederatedService.class);
    FederatedServiceCollection mockCollection = Mockito.mock(FederatedServiceCollectionImpl.class);
    Mockito.when(mockCollection.getServiceForAgencyIds(agencyIds)).thenReturn(mockService);

    Method method = SimpleFederatedService.class.getDeclaredMethod("getValueForValueAndIds", String.class, Set.class);
    Object[] args = {"value",ids};

    FederatedServiceMethodInvocationHandler handler = new FederatedByEntityIdsMethodInvocationHandlerImpl(1);
    handler.invoke(mockCollection, method, args);

    Mockito.verify(mockService).getValueForValueAndIds("value",ids);
  }
}
