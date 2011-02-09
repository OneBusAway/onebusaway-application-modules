package org.onebusaway.federations.annotations;

import org.onebusaway.federations.FederatedServiceCollection;
import org.onebusaway.federations.SimpleFederatedService;
import org.onebusaway.federations.impl.FederatedServiceCollectionImpl;

import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;


public class FederatedByBoundsMethodInvocationHandlerImplTest {

  @Test
  public void testSimple() throws Exception {

    SimpleFederatedService mockService = Mockito.mock(SimpleFederatedService.class);
    FederatedServiceCollection mockCollection = Mockito.mock(FederatedServiceCollectionImpl.class);
    Mockito.when(mockCollection.getServiceForBounds(0.0,1.0,2.0,3.0)).thenReturn(mockService);
    
    Method method = SimpleFederatedService.class.getDeclaredMethod("getValueForBounds", Double.TYPE,Double.TYPE,Double.TYPE,Double.TYPE);
    Object[] args = {0.0,1.0,2.0,3.0};

    FederatedServiceMethodInvocationHandler handler = new FederatedByBoundsMethodInvocationHandlerImpl(0,1,2,3);
    handler.invoke(mockCollection, method, args);

    Mockito.verify(mockService).getValueForBounds(0.0,1.0,2.0,3.0);
  }

}
