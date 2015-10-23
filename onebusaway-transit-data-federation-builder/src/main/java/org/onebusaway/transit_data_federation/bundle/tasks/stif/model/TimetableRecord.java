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
package org.onebusaway.transit_data_federation.bundle.tasks.stif.model;

/**
 * The header for a schedule for one bus line for one service period
 */
public class TimetableRecord implements StifRecord {

	private String routeIdentifier;
	private ServiceCode serviceCode;
  private String agencyId;

	public void setRouteIdentifier(String routeIdentifier) {
		this.routeIdentifier = routeIdentifier;
	}
	
	public String getRouteIdentifier() {
		return routeIdentifier;
	}

	public void setServiceCode(ServiceCode code) {
		this.serviceCode = code;
	}
	
	public ServiceCode getServiceCode() {
		return serviceCode;
	}

  public void setAgencyId(String agencyId) {
    this.agencyId = agencyId;
  }

  public String getAgencyId() {
    return agencyId;
  }
}
