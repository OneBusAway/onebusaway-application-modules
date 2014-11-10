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
package org.onebusaway.watchdog.api.schedule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.onebusaway.watchdog.api.MetricResource;
import org.springframework.beans.factory.annotation.Autowired;

@Path("/metric/schedule/agency")
public class AgencyResource extends MetricResource {
	static Map<String, Date> agencyEndDateMap = new HashMap<String, Date>();
	private CalendarService _calendarService;
	private TransitGraphDao _graph; 
	
	@Autowired
	public void setCalendarService(CalendarService calendarService) {
	  _calendarService = calendarService;
	}

	@Autowired
    public void setTransitGraphDao(TransitGraphDao graph) {
      _graph = graph;
    }

  
  @Path("/total")
  @GET
  public Response getAgencyCount() {
    try {
      int count = getTDS().getAgenciesWithCoverage().size();
      return Response.ok(ok("agency-count", count)).build();
    } catch (Exception e) {
      _log.error("getAgencyCount broke", e);
      return Response.ok(error("agency-count", e)).build();
    }
  }
  
  @Path("/id-list")
  @GET
  public Response getAgencyIdList() {
    try {
      
      List<AgencyWithCoverageBean> agencyBeans = getTDS().getAgenciesWithCoverage();
      List<String> agencyIds = new ArrayList<String>();
      for (AgencyWithCoverageBean agency : agencyBeans) {
        agencyIds.add(agency.getAgency().getId());
      }
      return Response.ok(ok("agency-id-list", agencyIds)).build();
    } catch (Exception e) {
      _log.error("getAgencyIdList broke", e);
      return Response.ok(error("agency-id-list", e)).build();
    }
  }
  
  @Path("/{agencyId}/expiry-date-delta")
  @GET
  public Response getAgencyExpiryDateDelta(@PathParam("agencyId") String agencyId) {
	  try {
		 Date endDate = agencyEndDateMap.get(agencyId);
		 if (endDate == null) {		  
			 List<TripEntry> trips = _graph.getAllTrips();
			 Set<AgencyAndId> tripSvcIds = new HashSet<AgencyAndId>();
			 for (TripEntry trip : trips) {
			   if (trip.getRoute().getId().getAgencyId().equals(agencyId)) {
			       tripSvcIds.add(trip.getServiceId().getId());
			   }
			 }
			 
			 Set<ServiceDate> serviceDates = new HashSet<ServiceDate>();
			 
			 for (AgencyAndId serviceId : tripSvcIds) {
				 serviceDates.addAll(_calendarService.getServiceDatesForServiceId(serviceId));
			 }
			 ServiceDate[] serviceDateArray = serviceDates.toArray(new ServiceDate[serviceDates.size()]);
			 Arrays.sort(serviceDateArray);
			 
			 endDate = serviceDateArray[serviceDateArray.length-1].getAsDate();
			 agencyEndDateMap.put(agencyId, endDate);
		 }
		 Calendar today = Calendar.getInstance();
		 today.set(Calendar.HOUR_OF_DAY, 0);
		 today.set(Calendar.MINUTE, 0);
		 today.set(Calendar.SECOND, 0);
		 today.set(Calendar.MILLISECOND, 0);
		 		 
		 long delta = (endDate.getTime() - (today.getTimeInMillis())) / ((1000 * 60 * 60 * 24));
		 		 
		  return Response.ok(ok("agency-expiry-date-delta",  delta)).build();
	  } catch (Exception e) {
	      _log.error("getAgencyExpiryDateDelta broke", e);
	      return Response.ok(error("agency-expiry-date-delta", e)).build();	  
	  } 
  }
}
