package org.onebusaway.transit_data_federation.impl.beans;

import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.schedule.StopTimeBean;
import org.onebusaway.transit_data_federation.services.beans.StopBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopTimeBeanService;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class StopTimeBeanServiceImpl implements StopTimeBeanService {

  private StopBeanService _stopBeanService;

  @Autowired
  public void setStopBeanService(StopBeanService stopBeanService) {
    _stopBeanService = stopBeanService;
  }

  @Override
  public StopTimeBean getStopTimeAsBean(StopTimeEntry stopTime) {

    StopTimeBean bean = new StopTimeBean();
    bean.setArrivalTime(stopTime.getArrivalTime());
    bean.setDepartureTime(stopTime.getDepartureTime());
    bean.setDropOffType(stopTime.getDropOffType());
    bean.setPickupType(stopTime.getPickupType());

    StopBean stopBean = _stopBeanService.getStopForId(stopTime.getStop().getId());
    bean.setStop(stopBean);

    return bean;
  }
}
