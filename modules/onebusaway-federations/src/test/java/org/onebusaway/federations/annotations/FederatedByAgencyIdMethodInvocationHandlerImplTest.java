package org.onebusaway.federations.annotations;

import org.onebusaway.federations.FederatedServiceRegistry;
import org.onebusaway.federations.SimpleFederatedService;

import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;

public class FederatedByAgencyIdMethodInvocationHandlerImplTest {

  @Test
  public void testSimple() throws Exception {

    SimpleFederatedService mockService = Mockito.mock(SimpleFederatedService.class);
    FederatedServiceRegistry mockRegistry = Mockito.mock(FederatedServiceRegistry.class);
    Mockito.when(mockRegistry.getServiceForAgencyId("agency")).thenReturn(mockService);
    
    Method method = SimpleFederatedService.class.getDeclaredMethod("getValueForId", String.class);
    Object[] args = {"agency_entity"};

    FederatedServiceMethodInvocationHandler handler = new FederatedByAgencyIdMethodInvocationHandlerImpl(0);
    handler.invoke(mockRegistry, method, args);

    Mockito.verify(mockService).getValueForId("agency_entity");
  }
  
  @Test
  public void testArgumentIndex() throws Exception {

    SimpleFederatedService mockService = Mockito.mock(SimpleFederatedService.class);
    FederatedServiceRegistry mockRegistry = Mockito.mock(FederatedServiceRegistry.class);
    Mockito.when(mockRegistry.getServiceForAgencyId("agency")).thenReturn(mockService);
    
    Method method = SimpleFederatedService.class.getDeclaredMethod("getValueForValueAndId", String.class,String.class);
    Object[] args = {"value","agency_entity"};

    FederatedServiceMethodInvocationHandler handler = new FederatedByAgencyIdMethodInvocationHandlerImpl(1);
    handler.invoke(mockRegistry, method, args);

    Mockito.verify(mockService).getValueForValueAndId("value","agency_entity");
  }
}
