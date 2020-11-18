/**
 * Copyright (C) 2011 Metropolitan Transportation Authority
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
package org.onebusaway.enterprise.webapp.actions.m.model;

import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.util.AgencyAndIdLibrary;

import java.util.List;

/**
 * A stop on a route, the route being the top-level search result.
 * @author jmaki
 *
 */
public class StopOnRoute {

  private StopBean stop;
  
  private List<String> distanceAways;
  
  private List<String> vehicleIds; // in approximate order with distanceAways;
  
  private Boolean hasRealtime = true;
  
  public StopOnRoute(StopBean stop, List<String> distanceAways, Boolean hasRealtime, List<String> vehicleIds) {
    this.stop = stop;
    this.distanceAways = distanceAways;
    if(hasRealtime != null)
    	this.hasRealtime = hasRealtime;
    this.vehicleIds = vehicleIds;
  }
  
  public String getId() {
    return stop.getId();
  }
  
  public String getCodeOrId() {
	  if (stop.getCode() != null) {
		  return stop.getCode();
	  }
	  return getId();
  }
  
  public String getIdWithoutAgency() {
    return AgencyAndIdLibrary.convertFromString(getId()).getId();
  }
  
  public String getCodeOrIdWithoutAgency() {
	  if (stop.getCode() != null) {
		  return stop.getCode();
	  }
	  return getIdWithoutAgency();
  }
  
  public String getName() {
    return stop.getName();
  }
  
  public List<String> getDistanceAways() {
    return distanceAways;
  }
  
  public List<String> getVehicleIds() {
    return vehicleIds;
  }

public Boolean getHasRealtime() {
	return hasRealtime;
}

public void setHasRealtime(Boolean hasRealtime) {
	this.hasRealtime = hasRealtime;
}

}
