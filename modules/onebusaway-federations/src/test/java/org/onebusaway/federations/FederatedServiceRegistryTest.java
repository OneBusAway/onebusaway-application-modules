package org.onebusaway.federations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.onebusaway.exceptions.MultipleServiceAreasServiceException;
import org.onebusaway.exceptions.OutOfServiceAreaServiceException;
import org.onebusaway.exceptions.ServiceAreaServiceException;
import org.onebusaway.geospatial.model.CoordinateBounds;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FederatedServiceRegistryTest {

  @Test
  public void go() throws ServiceAreaServiceException {

    Map<FederatedService, Map<String, List<CoordinateBounds>>> providers = new HashMap<FederatedService, Map<String, List<CoordinateBounds>>>();

    SimpleFederatedService serviceA = Mockito.mock(SimpleFederatedService.class);

    Map<String, List<CoordinateBounds>> firstProviderAgenciesAndCoverage = new HashMap<String, List<CoordinateBounds>>();
    firstProviderAgenciesAndCoverage.put("a1", Arrays.asList(new CoordinateBounds(1, 1, 3, 3),
        new CoordinateBounds(2, 2, 4, 4)));
    firstProviderAgenciesAndCoverage.put("a2", Arrays.asList(new CoordinateBounds(2, 5, 4, 6)));
    providers.put(serviceA, firstProviderAgenciesAndCoverage);

    SimpleFederatedService serviceB = Mockito.mock(SimpleFederatedService.class);

    Map<String, List<CoordinateBounds>> secondProviderAgenciesAndCoverage = new HashMap<String, List<CoordinateBounds>>();
    secondProviderAgenciesAndCoverage.put("b1", Arrays.asList(new CoordinateBounds(5, 5, 7, 7),
        new CoordinateBounds(6, 6, 8, 8)));
    secondProviderAgenciesAndCoverage.put("b2", Arrays.asList(new CoordinateBounds(5, 2, 7, 4)));
    providers.put(serviceB, secondProviderAgenciesAndCoverage);

    FederatedServiceRegistry registry = new FederatedServiceRegistry(providers);

    Set<FederatedService> services = registry.getAllServices();
    assertEquals(2, services.size());
    assertTrue(services.contains(serviceA));
    assertTrue(services.contains(serviceB));

    assertEquals(serviceA, registry.getServiceForAgencyId("a1"));
    assertEquals(serviceA, registry.getServiceForAgencyId("a2"));
    assertEquals(serviceB, registry.getServiceForAgencyId("b1"));
    assertEquals(serviceB, registry.getServiceForAgencyId("b2"));

    try {
      registry.getServiceForAgencyId("dne");
      fail();
    } catch (OutOfServiceAreaServiceException ex) {

    }

    Set<String> agencyIds = new HashSet<String>();
    agencyIds.add("a1");
    assertEquals(serviceA, registry.getServiceForAgencyIds(agencyIds));

    agencyIds.clear();
    agencyIds.add("a1");
    agencyIds.add("a2");
    assertEquals(serviceA, registry.getServiceForAgencyIds(agencyIds));

    agencyIds.clear();
    agencyIds.add("b1");
    agencyIds.add("b2");
    assertEquals(serviceB, registry.getServiceForAgencyIds(agencyIds));

    agencyIds.clear();
    agencyIds.add("a1");
    agencyIds.add("b2");
    try {
      registry.getServiceForAgencyIds(agencyIds);
      fail();
    } catch (MultipleServiceAreasServiceException ex) {

    }

    assertEquals(serviceA, registry.getServiceForBounds(1, 1, 4.5, 4.5));
    assertEquals(serviceA, registry.getServiceForBounds(1, 1, 4.5, 9));

    assertEquals(serviceB, registry.getServiceForBounds(5, 5, 5.5, 5.5));
    assertEquals(serviceB, registry.getServiceForBounds(5.5, 1, 7.5, 3));

    try {
      registry.getServiceForBounds(2, 2, 6, 6);
      fail();
    } catch (MultipleServiceAreasServiceException ex) {

    }

    assertEquals(serviceA, registry.getServiceForLocation(1.5, 1.5));
    assertEquals(serviceA, registry.getServiceForLocation(3, 5.5));

    assertEquals(serviceB, registry.getServiceForLocation(5.5, 5.5));
    assertEquals(serviceB, registry.getServiceForLocation(5.5, 3));

    try {
      registry.getServiceForLocation(0, 0);
      fail();
    } catch (OutOfServiceAreaServiceException ex) {

    }

  }
}
