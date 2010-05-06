package edu.washington.cs.rse.transit.common.impl.mybus;

import edu.washington.cs.rse.transit.common.model.aggregate.BusArrivalEstimateBean;
import edu.washington.cs.rse.transit.common.services.NoSuchStopException;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class MyBusWebPageServiceImplTest {

  @Test
  public void go() throws IOException, NoSuchStopException {
    MyBusWebPageServiceImpl service = new MyBusWebPageServiceImpl();
    List<BusArrivalEstimateBean> beans = service.getSchedule(316);
    for (BusArrivalEstimateBean bean : beans)
      System.out.println(bean + " deviation=" + bean.getGoalDeviation());
  }
}
