/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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

import java.util.TimeZone;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.TripStopTimeBean;
import org.onebusaway.transit_data.model.TripStopTimesBean;
import org.onebusaway.transit_data.model.schedule.FrequencyBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data_federation.services.AgencyService;
import org.onebusaway.transit_data_federation.services.beans.StopBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripStopTimesBeanService;
import org.onebusaway.transit_data_federation.services.blocks.BlockTripInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TripStopTimesBeanServiceImpl implements TripStopTimesBeanService {

  private TripBeanService _tripBeanService;

  private StopBeanService _stopBeanService;

  private AgencyService _agencyService;

  @Autowired
  public void setAgencyService(AgencyService agencyService) {
    _agencyService = agencyService;
  }

  @Autowired
  public void setTripBeanService(TripBeanService tripBeanService) {
    _tripBeanService = tripBeanService;
  }

  @Autowired
  public void setStopBeanService(StopBeanService stopBeanService) {
    _stopBeanService = stopBeanService;
  }

  @Override
  public TripStopTimesBean getStopTimesForBlockTrip(BlockTripInstance blockTripInstance) {

    BlockTripEntry blockTrip = blockTripInstance.getBlockTrip();
    TripStopTimesBean bean = getStopTimesForTrip(blockTrip.getTrip());

    if (blockTrip.getPreviousTrip() != null) {
      BlockTripEntry previous = blockTrip.getPreviousTrip();
      TripBean previousTrip = _tripBeanService.getTripForId(previous.getTrip().getId());
      bean.setPreviousTrip(previousTrip);
    }

    if (blockTrip.getNextTrip() != null) {
      BlockTripEntry next = blockTrip.getNextTrip();
      TripBean nextTrip = _tripBeanService.getTripForId(next.getTrip().getId());
      bean.setNextTrip(nextTrip);
    }
    
    FrequencyEntry frequencyLabel = blockTripInstance.getFrequencyLabel();
    
    if( frequencyLabel != null) {
      long serviceDate = blockTripInstance.getServiceDate();
      FrequencyBean fb = FrequencyBeanLibrary.getBeanForFrequency(serviceDate, frequencyLabel);
      bean.setFrequency(fb);
    }

    return bean;
  }

  /****
   * Private Methods
   ****/

  private TripStopTimesBean getStopTimesForTrip(TripEntry trip) {

    AgencyAndId tripId = trip.getId();

    TripStopTimesBean bean = new TripStopTimesBean();

    TimeZone tz = _agencyService.getTimeZoneForAgencyId(tripId.getAgencyId());
    bean.setTimeZone(tz.getID());

    for (StopTimeEntry stopTime : trip.getStopTimes()) {

      TripStopTimeBean stBean = new TripStopTimeBean();

      stBean.setArrivalTime(stopTime.getArrivalTime());
      stBean.setDepartureTime(stopTime.getDepartureTime());
      stBean.setStopHeadsign(stopTime.getStopHeadsign());

      StopEntry stopEntry = stopTime.getStop();
      StopBean stopBean = _stopBeanService.getStopForId(stopEntry.getId());
      stBean.setStop(stopBean);
      stBean.setDistanceAlongTrip(stopTime.getShapeDistTraveled());
      bean.addStopTime(stBean);
    }

    return bean;
  }
}
