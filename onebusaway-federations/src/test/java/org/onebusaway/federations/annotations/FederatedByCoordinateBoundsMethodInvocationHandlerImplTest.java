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

import org.onebusaway.federations.CoordinateBoundsTestBean;
import org.onebusaway.federations.FederatedServiceCollection;
import org.onebusaway.federations.SimpleFederatedService;
import org.onebusaway.federations.impl.FederatedServiceCollectionImpl;
import org.onebusaway.geospatial.model.CoordinateBounds;

import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;


public class FederatedByCoordinateBoundsMethodInvocationHandlerImplTest {

  @Test
  public void test01() throws Exception {
    
    CoordinateBounds bounds = new CoordinateBounds(0,1,2,3);

    SimpleFederatedService mockService = Mockito.mock(SimpleFederatedService.class);
    FederatedServiceCollection mockCollection = Mockito.mock(FederatedServiceCollectionImpl.class);
    Mockito.when(mockCollection.getServiceForBounds(bounds)).thenReturn(mockService);
    
    Method method = SimpleFederatedService.class.getDeclaredMethod("getValueForCoordinateBounds", CoordinateBounds.class);
    Object[] args = {bounds};

    FederatedServiceMethodInvocationHandler handler = new FederatedByCoordinateBoundsMethodInvocationHandlerImpl(method,0,"");
    handler.invoke(mockCollection, method, args);

    Mockito.verify(mockService).getValueForCoordinateBounds(bounds);
  }
  
  @Test
  public void test02() throws Exception {
    
    CoordinateBounds bounds = new CoordinateBounds(0,1,2,3);
    CoordinateBoundsTestBean bean = new CoordinateBoundsTestBean();
    bean.setBounds(bounds);
    
    SimpleFederatedService mockService = Mockito.mock(SimpleFederatedService.class);
    FederatedServiceCollection mockCollection = Mockito.mock(FederatedServiceCollectionImpl.class);
    Mockito.when(mockCollection.getServiceForBounds(bounds)).thenReturn(mockService);
    
    Method method = SimpleFederatedService.class.getDeclaredMethod("getValueForCoordinateBoundsTestBean", CoordinateBoundsTestBean.class);
    Object[] args = {bean};

    FederatedServiceMethodInvocationHandler handler = new FederatedByCoordinateBoundsMethodInvocationHandlerImpl(method,0,"bounds");
    handler.invoke(mockCollection, method, args);

    Mockito.verify(mockService).getValueForCoordinateBoundsTestBean(bean);
  }

}
