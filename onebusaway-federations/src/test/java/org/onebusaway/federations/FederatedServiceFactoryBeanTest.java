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
package org.onebusaway.federations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.federations.impl.LazyFederatedServiceCollectionImpl;
import org.onebusaway.geospatial.model.CoordinateBounds;

public class FederatedServiceFactoryBeanTest {

  @Test
  public void go() throws Exception {

    SimpleFederatedService serviceA = Mockito.mock(SimpleFederatedService.class);
    SimpleFederatedService serviceB = Mockito.mock(SimpleFederatedService.class);
    
    Map<String, List<CoordinateBounds>> firstProviderAgenciesAndCoverage = new HashMap<String, List<CoordinateBounds>>();
    firstProviderAgenciesAndCoverage.put("a1", Arrays.asList(new CoordinateBounds(1, 1, 3, 3),
        new CoordinateBounds(2, 2, 4, 4)));
    firstProviderAgenciesAndCoverage.put("a2", Arrays.asList(new CoordinateBounds(2, 5, 4, 6)));

    Map<String, List<CoordinateBounds>> secondProviderAgenciesAndCoverage = new HashMap<String, List<CoordinateBounds>>();
    secondProviderAgenciesAndCoverage.put("b1", Arrays.asList(new CoordinateBounds(5, 5, 7, 7),
        new CoordinateBounds(6, 6, 8, 8)));
    secondProviderAgenciesAndCoverage.put("b2", Arrays.asList(new CoordinateBounds(5, 2, 7, 4)));
    
    Mockito.when(serviceA.getAgencyIdsWithCoverageArea()).thenReturn(firstProviderAgenciesAndCoverage);
    Mockito.when(serviceB.getAgencyIdsWithCoverageArea()).thenReturn(secondProviderAgenciesAndCoverage);
    
    LazyFederatedServiceCollectionImpl collection = new LazyFederatedServiceCollectionImpl();
    collection.setServiceInterface(SimpleFederatedService.class);
    collection.setServiceProviders(Arrays.asList(serviceA,serviceB));

    FederatedServiceFactoryBean factory = new FederatedServiceFactoryBean();
    factory.setCollection(collection);
    factory.setServiceInterface(SimpleFederatedService.class);
    factory.afterPropertiesSet();
    SimpleFederatedService service = (SimpleFederatedService) factory.getObject();
    
    service.getValueForId("a1_test1");
    service.getValueForId("a2_test2");
    service.getValueForId("b1_test3");
    service.getValueForId("b2_test4");
    
    Mockito.verify(serviceA).getValueForId("a1_test1");
    Mockito.verify(serviceA).getValueForId("a2_test2");
    Mockito.verify(serviceB).getValueForId("b1_test3");
    Mockito.verify(serviceB).getValueForId("b2_test4");
  }
}
