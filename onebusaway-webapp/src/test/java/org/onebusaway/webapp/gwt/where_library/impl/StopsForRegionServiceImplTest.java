package org.onebusaway.webapp.gwt.where_library.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.transit_data.model.SearchQueryBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopsBean;
import org.onebusaway.webapp.gwt.where_library.rpc.WebappServiceAsync;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class StopsForRegionServiceImplTest {

  private StopsForRegionServiceImpl _service;

  // @BeforeClass
  public static void setupClass() {
    GWTMockLibrary.enable();
  }

  // @AfterClass
  public static void teardownClass() {
    GWTMockLibrary.disable();
  }

  // @Before
  public void setup() {
    _service = new StopsForRegionServiceImpl();
  }

  @Test
  public void noTest() {

  }

  // @Test
  // This test is disabled until we figure out how to setup mocking correctly
  public void test() {

    WebappServiceAsync webapp = _service.getWebappService();
    Go go = new Go();

    Mockito.doAnswer(go).when(webapp).getStops(Mockito.<SearchQueryBean> any(),
        Mockito.<AsyncCallback<StopsBean>> any());
    CoordinateBounds bounds = new CoordinateBounds(0.04, 0.04, 0.6, 0.6);
    StopsHandler handler = new StopsHandler();

    _service.getStopsForRegion(bounds, handler);

    assertEquals(0, handler.getStops());
  }

  private static class StopsHandler implements AsyncCallback<List<StopBean>> {

    private List<StopBean> _stops = new ArrayList<StopBean>();

    public List<StopBean> getStops() {
      return _stops;
    }

    @Override
    public void onSuccess(List<StopBean> stops) {
      _stops.addAll(stops);
    }

    @Override
    public void onFailure(Throwable arg0) {
      // TODO Auto-generated method stub

    }

  }

  private class Go implements Answer<Object> {

    @SuppressWarnings("unchecked")
    @Override
    public Object answer(InvocationOnMock invocation) throws Throwable {
      Object[] arguments = invocation.getArguments();
      CoordinateBounds bounds = (CoordinateBounds) arguments[0];
      AsyncCallback<StopsBean> callback = (AsyncCallback<StopsBean>) arguments[2];
      StopsBean stopsBean = new StopsBean();
      stopsBean.setLimitExceeded(false);
      callback.onSuccess(stopsBean);
      return null;
    }
  }
}
