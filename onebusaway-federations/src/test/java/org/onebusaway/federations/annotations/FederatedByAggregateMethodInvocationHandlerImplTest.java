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

import static org.junit.Assert.assertEquals;

import org.onebusaway.federations.FederatedService;
import org.onebusaway.federations.FederatedServiceCollection;
import org.onebusaway.federations.SimpleFederatedService;
import org.onebusaway.federations.impl.FederatedServiceCollectionImpl;

import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FederatedByAggregateMethodInvocationHandlerImplTest {
  
  @SuppressWarnings("unchecked")
  @Test
  public void testList() throws Exception {

    SimpleFederatedService mockServiceA = Mockito.mock(SimpleFederatedService.class);
    Mockito.when(mockServiceA.getValuesAsList()).thenReturn(Arrays.asList("a","b"));
    
    SimpleFederatedService mockServiceB = Mockito.mock(SimpleFederatedService.class);
    Mockito.when(mockServiceB.getValuesAsList()).thenReturn(Arrays.asList("c","d", "a"));
    
    Set<FederatedService> services = new HashSet<FederatedService>();
    services.add(mockServiceA);
    services.add(mockServiceB);
    
    FederatedServiceCollection mockCollection = Mockito.mock(FederatedServiceCollectionImpl.class);
    Mockito.when(mockCollection.getAllServices()).thenReturn(services);

    Method method = SimpleFederatedService.class.getDeclaredMethod("getValuesAsList");

    FederatedServiceMethodInvocationHandler handler = new FederatedByAggregateMethodInvocationHandlerImpl(EMethodAggregationType.LIST);
    List<String> results = (List<String>) handler.invoke(mockCollection, method, new Object[] {});

    Mockito.verify(mockServiceA).getValuesAsList();
    Mockito.verify(mockServiceB).getValuesAsList();

    assertEquals(5,results.size());
    Collections.sort(results);
    assertEquals("a",results.get(0));
    assertEquals("a",results.get(1));
    assertEquals("b",results.get(2));
    assertEquals("c",results.get(3));
    assertEquals("d",results.get(4));
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void testMap() throws Exception {

    Map<String,String> vA = new HashMap<String, String>();
    vA.put("a","a1");
    vA.put("b","b2");
    
    Map<String,String> vB = new HashMap<String, String>();
    vB.put("c","c3");
    vB.put("d","d4");
    
    SimpleFederatedService mockServiceA = Mockito.mock(SimpleFederatedService.class);
    Mockito.when(mockServiceA.getValuesAsMap()).thenReturn(vA);
    
    SimpleFederatedService mockServiceB = Mockito.mock(SimpleFederatedService.class);
    Mockito.when(mockServiceB.getValuesAsMap()).thenReturn(vB);
    
    Set<FederatedService> services = new HashSet<FederatedService>();
    services.add(mockServiceA);
    services.add(mockServiceB);
    
    FederatedServiceCollection mockCollection = Mockito.mock(FederatedServiceCollectionImpl.class);
    Mockito.when(mockCollection.getAllServices()).thenReturn(services);

    Method method = SimpleFederatedService.class.getDeclaredMethod("getValuesAsMap");

    FederatedServiceMethodInvocationHandler handler = new FederatedByAggregateMethodInvocationHandlerImpl(EMethodAggregationType.MAP);
    Map<String,String> results = (Map<String,String>) handler.invoke(mockCollection, method, new Object[] {});

    Mockito.verify(mockServiceA).getValuesAsMap();
    Mockito.verify(mockServiceB).getValuesAsMap();

    assertEquals(4,results.size());
    assertEquals("a1",results.get("a"));
    assertEquals("b2",results.get("b"));
    assertEquals("c3",results.get("c"));
    assertEquals("d4",results.get("d"));
  }
}
