package org.onebusaway.federations.annotations;

import org.onebusaway.federations.FederatedServiceRegistry;
import org.onebusaway.federations.SimpleFederatedService;

import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;


public class FederatedByLocationMethodInvocationHandlerImplTest {
  
  @Test
  public void testSimple() throws Exception {

    SimpleFederatedService mockService = Mockito.mock(SimpleFederatedService.class);
    FederatedServiceRegistry mockRegistry = Mockito.mock(FederatedServiceRegistry.class);
    Mockito.when(mockRegistry.getServiceForLocation(0.0,1.0)).thenReturn(mockService);
    
    Method method = SimpleFederatedService.class.getDeclaredMethod("getValueForLocation", Double.TYPE,Double.TYPE);
    Object[] args = {0.0,1.0};

    FederatedServiceMethodInvocationHandler handler = new FederatedByLocationMethodInvocationHandlerImpl(0,1);
    handler.invoke(mockRegistry, method, args);

    Mockito.verify(mockService).getValueForLocation(0.0,1.0);
  }
}
