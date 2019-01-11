/**
 * Copyright (C) 2018 Cambridge Systematics, Inc.
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
package org.onebusaway.api.actions.api.where;

import com.opensymphony.xwork2.conversion.annotations.TypeConversion;
import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.model.transit.BeanFactoryV2;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.OccupancyStatus;
import org.onebusaway.transit_data.HistoricalRidershipBean;
import org.onebusaway.transit_data.OccupancyStatusBean;
import org.onebusaway.transit_data.model.*;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.model.bundle.HistoricalRidership;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.util.SystemTime;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HistoricalOccupancyByStopAction extends ApiActionSupport {
  private static final long serialVersionUID = 1L;
  private static final int V2 = 2;

//  private String _id;
//
//  private String _agencyId;

  private HistoricalOccupancyByStopQueryBean _query = new HistoricalOccupancyByStopQueryBean();

  public HistoricalOccupancyByStopAction() { super(V2); }

  @RequiredFieldValidator
  public void setId(String id) {
    _query.setStopId(id);
  }

  public String getId() {
    return _query.getStopId();
  }

  @TypeConversion(converter = "org.onebusaway.presentation.impl.conversion.DateConverter")
  public void setServiceDate(Date date) {
    _query.setServiceDate(date.getTime());
  }

  public long getServiceDate() {
    return _query.getServiceDate();
  }

  public void setTripId(String tripId) { _query.setTripId(tripId); }

  public String getTripId() { return _query.getTripId(); }

  public void setRouteId(String routeId) { _query.setRouteId(routeId); }

  public String getRouteId() { return _query.getRouteId(); }

  @Autowired
  private TransitDataService _service;

  public DefaultHttpHeaders show() throws IOException, ServiceException {
    if (!isVersion(V2))
       return setUnknownVersionResponse();
    if (hasErrors())
       return setValidationErrorsResponse();

    AgencyAndId routeId = AgencyAndId.convertFromString(_query.getRouteId());
    AgencyAndId tripId = AgencyAndId.convertFromString(_query.getTripId());
    AgencyAndId stopId = AgencyAndId.convertFromString(_query.getStopId());

    /**
     * Attempting to make this customizable.
     * If a route and/or a trip is in the query,
     *  then return only the ridership values for the stop
     *  which are associated with them
     */
    List<HistoricalRidershipBean> hrs = new ArrayList<>();
    List<HistoricalRidershipBean> fil = new ArrayList<>();

    // If we have both route and trip, get specific ridership
    if (_query.getRouteId() != null && _query.getTripId() != null) {
      hrs = _service.getHistoricalRiderships(routeId, tripId, stopId);
    } else {
      // Otherwise, get all ridership for this stop, and filter with whats given
      hrs = _service.getHistoricalRidershipForStop(_query);
      // filter for given route
      if (_query.getRouteId() != null) {
        for( HistoricalRidershipBean hr : hrs ) {
          if(hr.getRouteId().getId().equals(routeId.getId())) {
            fil.add(hr);
          }
        }
        hrs = new ArrayList<>(fil);
      }
      // filter for given trip
      else if(_query.getTripId() != null) {
        for( HistoricalRidershipBean hr : hrs ) {
          if(hr.getTripId().getId().equals(tripId.getId())) {
            fil.add(hr);
          }
        }
        hrs = new ArrayList<>(fil);
      }
    }
    // Then filter for the Service Date
    if(_query.getServiceDate() != 0) {
      // Gather the list of routes, from the schedule for this Stop, on this Service Date
      StopScheduleBean schedule = _service.getScheduleForStop(stopId.toString(), new Date(_query.getServiceDate()));
      List<String> schedRoutes = new ArrayList<>();
      for (StopRouteScheduleBean srsb : schedule.getRoutes()) {
        schedRoutes.add(srsb.getRoute().getId());
      }
      fil = new ArrayList<>();
      for (HistoricalRidershipBean hr : hrs) {
        if (schedRoutes.contains(hr.getRouteId().toString())) {
          fil.add(hr);
        }
      }
      hrs = new ArrayList<>(fil);
    }


    // convert to OccupancyStatus enums
    List<OccupancyStatusBean> occ = new ArrayList<>();
    for (HistoricalRidershipBean hr : hrs) {
      OccupancyStatusBean bean = new OccupancyStatusBean();
      bean.setStatus(OccupancyStatus.toEnum(hr.getLoadFactor()));
      occ.add(bean);
    }

    BeanFactoryV2 factory = getBeanFactoryV2();

    return setOkResponse(factory.getHistoricalOccupancyResponse(occ));
  }

}
