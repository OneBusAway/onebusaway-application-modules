package org.onebusaway.nextbus.actions.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.nextbus.model.Body;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.springframework.beans.factory.annotation.Autowired;

public class NextBusApiBase {
	@Autowired
	protected TransitDataService _transitDataService;
	
	protected boolean isValidRoute(AgencyAndId routeId) {
	    if (routeId != null && routeId.hasValues() && this._transitDataService.getRouteForId(routeId.toString()) != null) {
	      return true;
	    }
	    return false;
	}
	
	protected boolean isValidStop(AgencyAndId stopId) {
		try {
			StopBean stopBean = _transitDataService
					.getStop(stopId.toString());
			if (stopBean != null)
				return true;
		} catch (Exception e) {
			// This means the stop id is not valid.
		}
		return false;
	}

	protected List<String> getAgencies(String agencyIdVal) {
		String agencyId = agencyIdVal;
		List<String> agencyIds = new ArrayList<String>();
		if (agencyId != null) {
			// The user provided an agancy id so, use it
			agencyIds.add(agencyId);
		} else {
			// They did not provide an agency id, so interpret that an any/all
			// agencies.
			Map<String, List<CoordinateBounds>> agencies = _transitDataService
					.getAgencyIdsWithCoverageArea();
			agencyIds.addAll(agencies.keySet());
		}
		return agencyIds;
	}
	
	protected List<String> processAgencyIds(String agencyId){
		List<String> agencyIds = new ArrayList<String>();
		
		// Try to get the agency id passed by the user
		if (agencyId != null) {
			// The user provided an agancy id so, use it
			agencyIds.add(agencyId);
		} else {
			// They did not provide an agency id, so interpret that an any/all
			// agencies.
			Map<String, List<CoordinateBounds>> agencies = _transitDataService
					.getAgencyIdsWithCoverageArea();
			agencyIds.addAll(agencies.keySet());
		}
		
		return agencyIds;
	}
	
	protected List<AgencyAndId> processVehicleIds(String vehicleRef, List<String> agencyIds){
		List<AgencyAndId> vehicleIds = new ArrayList<AgencyAndId>();
	    if (vehicleRef != null) {
	      try {
	        // If the user included an agency id as part of the vehicle id, ignore any OperatorRef arg
	        // or lack of OperatorRef arg and just use the included one.
	        AgencyAndId vehicleId = AgencyAndIdLibrary.convertFromString(vehicleRef);
	        vehicleIds.add(vehicleId);
	      } catch (Exception e) {
	        // The user didn't provide an agency id in the VehicleRef, so use our list of operator refs
	        for (String agency : agencyIds) {
	          AgencyAndId vehicleId = new AgencyAndId(agency, vehicleRef);
	          vehicleIds.add(vehicleId);
	        }
	      }
	    }
		
		return vehicleIds;
	}
	
	protected <E> void processRouteIds(String routeVal, List<AgencyAndId> routeIds, List<String> agencyIds, Body<E> body) {
		if (StringUtils.isNotBlank(routeVal)) {
			try {
				AgencyAndId routeId = AgencyAndIdLibrary
						.convertFromString(routeVal);
				if (this.isValidRoute(routeId)) {
					routeIds.add(routeId);
				} else {
					body.getErrors().add("No such route: " + routeId.toString() + ".");
				}
			} catch (Exception e) {
				for (String agency : agencyIds) {
					AgencyAndId routeId = new AgencyAndId(agency, routeVal);
					if (this.isValidRoute(routeId)) {
						routeIds.add(routeId);
					} else {
						body.getErrors().add("No such route: " + routeId.toString() + ". ");
					}
				}
			}
		}
		else{
			body.getErrors().add("You must provide a route id.");

		}
		
	}
	
	
	protected <E> void processStopIds(String stopIdVal, List<AgencyAndId> stopIds, List<String> agencyIds, Body<E> body){
		
	    if (StringUtils.isNotBlank(stopIdVal)) {
	      try {
	        // If the user included an agency id as part of the stop id, ignore any OperatorRef arg
	        // or lack of OperatorRef arg and just use the included one.
	        AgencyAndId stopId = AgencyAndIdLibrary.convertFromString(stopIdVal);
	        if (isValidStop(stopId)) {
	          stopIds.add(stopId);
	        } else {
	        	body.getErrors().add("No such stop: " + stopId.toString() + ". ");
	        }
	      } catch (Exception e) {
	        // The user didn't provide an agency id in the MonitoringRef, so use our list of operator refs
	        for (String agency : agencyIds) {
	          AgencyAndId stopId = new AgencyAndId(agency, stopIdVal);
	          if (isValidStop(stopId)) {
	            stopIds.add(stopId);
	          } else {
	        	  body.getErrors().add("No such stop: " + stopId.toString() + ". ");
	          }
	        }
	      }
	      
	      if (stopIds.size() == 0) 
	    	  body.getErrors().add("You must provide a StopId.");
	      }
	}
	  
	
}
