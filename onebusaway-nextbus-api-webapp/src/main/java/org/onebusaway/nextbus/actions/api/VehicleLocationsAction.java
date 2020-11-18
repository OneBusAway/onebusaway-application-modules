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
import java.util.concurrent.TimeUnit;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.apache.struts2.rest.HttpHeaders;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.nextbus.model.nextbus.Body;
import org.onebusaway.nextbus.model.nextbus.LastTime;
import org.onebusaway.nextbus.model.nextbus.Vehicle;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsInclusionBean;
import org.onebusaway.transit_data.model.trips.TripStatusBean;
import org.onebusaway.transit_data.model.trips.TripsForAgencyQueryBean;
import org.onebusaway.transit_data.model.trips.TripsForRouteQueryBean;
import org.onebusaway.util.SystemTime;

import com.opensymphony.xwork2.ModelDriven;

public class VehicleLocationsAction extends NextBusApiBase implements
    ModelDriven<Body<Vehicle>> {

  private final int MAX_RESULTS = 100;

  private String agencyId;

  private String routeId;

  private long time;

  public String getA() {
    return agencyId;
  }

  public void setA(String agencyId) {
    this.agencyId = getMappedAgency(agencyId);
  }

  public String getR() {
    return routeId;
  }

  public void setR(String routeId) {
    this.routeId = _tdsMappingService.getRouteIdFromShortName(routeId);
  }

  public long getT() {
    if (time == 0)
      return SystemTime.currentTimeMillis();
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
      // Valid Route Specified
      if (routeIds.size() > 0) {
        for (AgencyAndId routeId : routeIds) {
          body.getResponse().addAll(
              getVehiclesForRoute(routeId.toString(),
                  getAllTripsForRoute(routeId.toString(), getT())));
        }
      } 
      // Invalid Route Specified, return results for first 100 results for agency
      else {
        body.getResponse().addAll(
            getVehiclesForRoute(null,
                getAllTripsForAgency(agencyId, getT())));
      }
      body.setLastTime(new LastTime(SystemTime.currentTimeMillis()));
    }

    return body;

  }

  private List<Vehicle> getVehiclesForRoute(String routeId,
      ListBean<TripDetailsBean> trips) {

    DecimalFormat df = new DecimalFormat();
    df.setMaximumFractionDigits(6);

    List<Vehicle> vehiclesList = new ArrayList<Vehicle>();
    for (TripDetailsBean tripDetails : trips.getList()) {
      if(tripDetails == null) continue;
      TripStatusBean tripStatus = tripDetails.getStatus();

      // filter out interlined routes
      if (routeId != null
          && !tripDetails.getTrip().getRoute().getId().equals(routeId))
        continue;

      Vehicle vehicle = new Vehicle();
      
      String vehicleId = null;
      
      if(tripStatus.getVehicleId() != null){
        vehicleId = getIdNoAgency(tripStatus.getVehicleId());
      }
      else
        continue;

      
      vehicle.setId(vehicleId);
      vehicle.setLat(new BigDecimal(
          df.format(tripStatus.getLocation().getLat())));
      vehicle.setLon(new BigDecimal(
          df.format(tripStatus.getLocation().getLon())));
      vehicle.setHeading((int) tripStatus.getOrientation());
      vehicle.setDirTag(tripStatus.getActiveTrip().getDirectionId());
      vehicle.setPredictable(tripStatus.isPredicted());
      vehicle.setRouteTag(getIdNoAgency(tripStatus.getActiveTrip().getRoute().getId()));
      vehicle.setTripTag(getIdNoAgency(tripStatus.getActiveTrip().getId()));
      vehicle.setBlock(getIdNoAgency(tripStatus.getActiveTrip().getBlockId()));

      int secondsSinceUpdate = 0;
      if (tripStatus.getLastUpdateTime() > 0)
        secondsSinceUpdate = (int) TimeUnit.MILLISECONDS.toSeconds(SystemTime.currentTimeMillis()
            - tripStatus.getLastUpdateTime());

      vehicle.setSecsSinceReport(secondsSinceUpdate);
      

      vehiclesList.add(vehicle);
    }

    return vehiclesList;

  }

  private ListBean<TripDetailsBean> getAllTripsForRoute(String routeId,
      long currentTime) {
    TripsForRouteQueryBean query = new TripsForRouteQueryBean();
    query.setRouteId(routeId);
    query.setTime(currentTime);

    TripDetailsInclusionBean inclusionBean = new TripDetailsInclusionBean();
    inclusionBean.setIncludeTripBean(true);
    inclusionBean.setIncludeTripStatus(true);
    query.setInclusion(inclusionBean);

    return _transitDataService.getTripsForRoute(query);
  }

  private ListBean<TripDetailsBean> getAllTripsForAgency(String agencyId,
      long currentTime) {
    TripsForAgencyQueryBean query = new TripsForAgencyQueryBean();
    query.setAgencyId(agencyId);
    query.setTime(currentTime);

    TripDetailsInclusionBean inclusionBean = new TripDetailsInclusionBean();
    inclusionBean.setIncludeTripBean(true);
    inclusionBean.setIncludeTripStatus(true);
    query.setInclusion(inclusionBean);

    return _transitDataService.getTripsForAgency(query);
  }

  private boolean isValid(Body body) {

    if (!isValidAgency(body, agencyId))
      return false;

    return true;
  }
}
