package org.onebusaway.federations.annotations;

import org.onebusaway.federations.EntityIdTestBean;
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
    Mockito.when(mockCollection.getServiceForAgencyId("agency")).thenReturn(
        mockService);

    Method method = SimpleFederatedService.class.getDeclaredMethod(
        "getValueForId", String.class);
    Object[] args = {"agency_entity"};

    FederatedServiceMethodInvocationHandler handler = new FederatedByEntityIdMethodInvocationHandlerImpl(
        method, 0, "");
    handler.invoke(mockCollection, method, args);

    Mockito.verify(mockService).getValueForId("agency_entity");
  }

  @Test
  public void testArgumentIndex() throws Exception {

    SimpleFederatedService mockService = Mockito.mock(SimpleFederatedService.class);
    FederatedServiceCollection mockCollection = Mockito.mock(FederatedServiceCollectionImpl.class);
    Mockito.when(mockCollection.getServiceForAgencyId("agency")).thenReturn(
        mockService);

    Method method = SimpleFederatedService.class.getDeclaredMethod(
        "getValueForValueAndId", String.class, String.class);
    Object[] args = {"value", "agency_entity"};

    FederatedServiceMethodInvocationHandler handler = new FederatedByEntityIdMethodInvocationHandlerImpl(
        method, 1, "");
    handler.invoke(mockCollection, method, args);

    Mockito.verify(mockService).getValueForValueAndId("value", "agency_entity");
  }

  @Test
  public void testPropertyExpression() throws Exception {

    SimpleFederatedService mockService = Mockito.mock(SimpleFederatedService.class);
    FederatedServiceCollection mockCollection = Mockito.mock(FederatedServiceCollectionImpl.class);
    Mockito.when(mockCollection.getServiceForAgencyId("agency")).thenReturn(
        mockService);

    Method method = SimpleFederatedService.class.getDeclaredMethod(
        "getValueForValueBean", EntityIdTestBean.class);

    EntityIdTestBean value = new EntityIdTestBean("agency_entity");
    Object[] args = {value};

    FederatedServiceMethodInvocationHandler handler = new FederatedByEntityIdMethodInvocationHandlerImpl(
        method, 0, "id");
    handler.invoke(mockCollection, method, args);

    Mockito.verify(mockService).getValueForValueBean(value);
  }
}
