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
package org.onebusaway.transit_data_federation.impl.beans;

import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopTimeInstanceBean;
import org.onebusaway.transit_data.model.schedule.StopTimeBean;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.beans.StopBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopTimeBeanService;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.model.StopTimeInstance;
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

    StopBean stopBean = _stopBeanService.getStopForId(stopTime.getStop().getId(), null);
    bean.setStop(stopBean);

    return bean;
  }
  
  @Override
  public StopTimeInstanceBean getStopTimeInstanceAsBean(StopTimeInstance instance) {
    StopTimeInstanceBean bean = new StopTimeInstanceBean();
    bean.setArrivalTime(instance.getArrivalTime());
    bean.setDepartureTime(instance.getDepartureTime());
    bean.setServiceDate(instance.getServiceDate());
    bean.setTripId(AgencyAndIdLibrary.convertToString(instance.getTrip().getTrip().getId()));
    return bean;
  }
}
