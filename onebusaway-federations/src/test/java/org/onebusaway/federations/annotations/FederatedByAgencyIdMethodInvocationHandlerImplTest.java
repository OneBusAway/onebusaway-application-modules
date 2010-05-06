package org.onebusaway.federations.annotations;

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
    Mockito.when(mockCollection.getServiceForAgencyId("agency")).thenReturn(mockService);
    
    Method method = SimpleFederatedService.class.getDeclaredMethod("getValueForId", String.class);
    Object[] args = {"agency_entity"};

    FederatedServiceMethodInvocationHandler handler = new FederatedByEntityIdMethodInvocationHandlerImpl(0);
    handler.invoke(mockCollection, method, args);

    Mockito.verify(mockService).getValueForId("agency_entity");
  }
  
  @Test
  public void testArgumentIndex() throws Exception {

    SimpleFederatedService mockService = Mockito.mock(SimpleFederatedService.class);
    FederatedServiceCollection mockCollection = Mockito.mock(FederatedServiceCollectionImpl.class);
    Mockito.when(mockCollection.getServiceForAgencyId("agency")).thenReturn(mockService);
    
    Method method = SimpleFederatedService.class.getDeclaredMethod("getValueForValueAndId", String.class,String.class);
    Object[] args = {"value","agency_entity"};

    FederatedServiceMethodInvocationHandler handler = new FederatedByEntityIdMethodInvocationHandlerImpl(1);
    handler.invoke(mockCollection, method, args);

    Mockito.verify(mockService).getValueForValueAndId("value","agency_entity");
  }
}
