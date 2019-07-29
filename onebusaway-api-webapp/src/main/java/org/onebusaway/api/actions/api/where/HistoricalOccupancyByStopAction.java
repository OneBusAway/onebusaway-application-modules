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
import org.onebusaway.transit_data.OccupancyStatusBean;
import org.onebusaway.transit_data.model.*;
import org.onebusaway.transit_data.services.TransitDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HistoricalOccupancyByStopAction extends ApiActionSupport {
  private static final long serialVersionUID = 1L;
  private static final int V2 = 2;

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
    long serviceDate = _query.getServiceDate();
    /**
     * Attempting to make this customizable.
     * If a route, trip, and/or service date is in the query,
     *  then return only the ridership values for the stop
     *  which are associated with them
     */
    List<OccupancyStatusBean> hrs = _service.getHistoricalRidershipForStop(_query);
    List<OccupancyStatusBean> fil = new ArrayList<>();

    // If we have both route and trip, get specific ridership
    if (_query.getRouteId() != null && _query.getTripId() != null) {
        hrs = _service.getHistoricalRiderships(routeId, tripId, stopId, serviceDate);

    // Otherwise, get all ridership for this stop, and filter with whats given
    } else {
      // filter for given route
      if (_query.getRouteId() != null) {
        for( OccupancyStatusBean occ : hrs ) {
          if(occ.getRouteId().getId().equals(routeId.getId())) {
            fil.add(occ);
          }
        }
        hrs = new ArrayList<>(fil);
      }
      // filter for given trip
      else if(_query.getTripId() != null) {
        for( OccupancyStatusBean occ : hrs ) {
          if(occ.getTripId().getId().equals(tripId.getId())) {
            fil.add(occ);
          }
        }
        hrs = new ArrayList<>(fil);
      }
    }

    BeanFactoryV2 factory = getBeanFactoryV2();

    return setOkResponse(factory.getHistoricalOccupancyResponse(hrs));
  }
}
