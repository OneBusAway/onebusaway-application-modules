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
