/**
 * Copyright (C) 2015 Cambridge Systematics
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
package org.onebusaway.nextbus.actions.api;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.apache.struts2.rest.HttpHeaders;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.nextbus.model.nextbus.Body;
import org.onebusaway.nextbus.model.nextbus.DisplayRoute;
import org.onebusaway.nextbus.model.nextbus.LastTime;
import org.onebusaway.nextbus.model.nextbus.Vehicle;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.realtime.VehicleLocationRecordQueryBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsInclusionBean;
import org.onebusaway.transit_data.model.trips.TripStatusBean;
import org.onebusaway.transit_data.model.trips.TripsForRouteQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;

public class VehicleLocationsAction extends NextBusApiBase implements
		ModelDriven<Body<Vehicle>> {

	private String agencyId;

	private String routeId;

	private long time;

	public String getA() {
		return agencyId;
	}

	public void setA(String agencyId) {
		this.agencyId = agencyId;
	}

	public String getR() {
		return routeId;
	}

	public void setR(String routeId) {
		this.routeId = routeId;
	}

	public long getT() {
		if (time == 0)
			return System.currentTimeMillis();
		return time;
	}

	public void setT(long time) {
		this.time = time;
	}

	public HttpHeaders index() {
		return new DefaultHttpHeaders("success");
	}

	@Override
	public Body<Vehicle> getModel() {

		Body<Vehicle> body = new Body<Vehicle>();
		List<AgencyAndId> routeIds = new ArrayList<AgencyAndId>();
		List<String> agencies = new ArrayList<String>();

		if (isValid(body)) {

			agencies.add(agencyId);
			processRouteIds(routeId, routeIds, agencies, body, false);

			if (routeIds.size() > 0) {
				for (AgencyAndId routeId : routeIds) {
					body.getResponse().addAll(
							getVehiclesForRoute(routeId.toString(), time));
				}

			} else {

				List<RouteBean> routeBeans = _transitDataService
						.getRoutesForAgencyId(agencyId).getList();

				for (RouteBean routeBean : routeBeans) {
					body.getResponse().addAll(
							getVehiclesForRoute(routeBean.getId(), time));
				}
			}
		}

		body.setLastTime(new LastTime(System.currentTimeMillis()));

		return body;

	}

	private List<Vehicle> getVehiclesForRoute(String routeId, long time) {

		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(6);

		List<Vehicle> vehiclesList = new ArrayList<Vehicle>();
		ListBean<TripDetailsBean> trips = getAllTripsForRoute(routeId, time);
		for (TripDetailsBean tripDetails : trips.getList()) {

			TripStatusBean tripStatus = tripDetails.getStatus();

			// filter out interlined routes
			if (routeId != null
					&& !tripDetails.getTrip().getRoute().getId()
							.equals(routeId))
				continue;

			Vehicle vehicle = new Vehicle();
			vehicle.setId(tripStatus.getVehicleId());
			vehicle.setLat(new BigDecimal(df.format(tripStatus.getLocation()
					.getLat())));
			vehicle.setLon(new BigDecimal(df.format(tripStatus.getLocation()
					.getLon())));
			vehicle.setHeading((int) tripStatus.getOrientation());
			vehicle.setDirTag(tripStatus.getActiveTrip().getDirectionId());
			vehicle.setPredictable(tripStatus.isPredicted());
			vehicle.setRouteTag(getIdNoAgency(tripStatus.getActiveTrip()
					.getRoute().getId()));

			int secondsSinceUpdate = 0;
			if (tripStatus.getLastUpdateTime() > 0)
				secondsSinceUpdate = (int) TimeUnit.MILLISECONDS
						.toSeconds(System.currentTimeMillis()
								- tripStatus.getLastUpdateTime());

			vehicle.setSecsSinceReport(secondsSinceUpdate);

			vehiclesList.add(vehicle);
		}

		return vehiclesList;

	}

	private ListBean<TripDetailsBean> getAllTripsForRoute(String routeId,
			long currentTime) {
		TripsForRouteQueryBean tripRouteQueryBean = new TripsForRouteQueryBean();
		tripRouteQueryBean.setRouteId(routeId);
		tripRouteQueryBean.setTime(currentTime);

		TripDetailsInclusionBean inclusionBean = new TripDetailsInclusionBean();
		inclusionBean.setIncludeTripBean(true);
		inclusionBean.setIncludeTripStatus(true);
		tripRouteQueryBean.setInclusion(inclusionBean);

		return _transitDataService.getTripsForRoute(tripRouteQueryBean);
	}

	private boolean isValid(Body body) {

		if (!isValidAgency(body, agencyId))
			return false;

		return true;
	}
}
