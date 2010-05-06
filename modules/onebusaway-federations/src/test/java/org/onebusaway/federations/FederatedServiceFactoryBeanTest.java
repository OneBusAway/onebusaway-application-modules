package org.onebusaway.federations;

import static org.junit.Assert.assertEquals;

import org.onebusaway.geospatial.model.CoordinateBounds;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    
    FederatedServiceFactoryBean factory = new FederatedServiceFactoryBean();
    factory.setServiceInterface(SimpleFederatedService.class);
    factory.setServiceProviders(Arrays.asList(serviceA,serviceB));
    
    assertEquals(SimpleFederatedService.class,factory.getObjectType());
    
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
