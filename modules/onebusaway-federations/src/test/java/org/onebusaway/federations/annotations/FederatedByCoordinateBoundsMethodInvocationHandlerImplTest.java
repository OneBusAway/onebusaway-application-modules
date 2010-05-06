package org.onebusaway.federations.annotations;

import org.onebusaway.federations.CoordinateBoundsTestBean;
import org.onebusaway.federations.FederatedServiceRegistry;
import org.onebusaway.federations.SimpleFederatedService;
import org.onebusaway.geospatial.model.CoordinateBounds;

import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;


public class FederatedByCoordinateBoundsMethodInvocationHandlerImplTest {

  @Test
  public void test01() throws Exception {
    
    CoordinateBounds bounds = new CoordinateBounds(0,1,2,3);

    SimpleFederatedService mockService = Mockito.mock(SimpleFederatedService.class);
    FederatedServiceRegistry mockRegistry = Mockito.mock(FederatedServiceRegistry.class);
    Mockito.when(mockRegistry.getServiceForBounds(bounds)).thenReturn(mockService);
    
    Method method = SimpleFederatedService.class.getDeclaredMethod("getValueForCoordinateBounds", CoordinateBounds.class);
    Object[] args = {bounds};

    FederatedServiceMethodInvocationHandler handler = new FederatedByCoordinateBoundsMethodInvocationHandlerImpl(method,0,"");
    handler.invoke(mockRegistry, method, args);

    Mockito.verify(mockService).getValueForCoordinateBounds(bounds);
  }
  
  @Test
  public void test02() throws Exception {
    
    CoordinateBounds bounds = new CoordinateBounds(0,1,2,3);
    CoordinateBoundsTestBean bean = new CoordinateBoundsTestBean();
    bean.setBounds(bounds);
    
    SimpleFederatedService mockService = Mockito.mock(SimpleFederatedService.class);
    FederatedServiceRegistry mockRegistry = Mockito.mock(FederatedServiceRegistry.class);
    Mockito.when(mockRegistry.getServiceForBounds(bounds)).thenReturn(mockService);
    
    Method method = SimpleFederatedService.class.getDeclaredMethod("getValueForCoordinateBoundsTestBean", CoordinateBoundsTestBean.class);
    Object[] args = {bean};

    FederatedServiceMethodInvocationHandler handler = new FederatedByCoordinateBoundsMethodInvocationHandlerImpl(method,0,"bounds");
    handler.invoke(mockRegistry, method, args);

    Mockito.verify(mockService).getValueForCoordinateBoundsTestBean(bean);
  }

}
