/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
package org.onebusaway.admin.model.json;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Holds vehicle pullout/pullin information
 * @author abelsare
 *
 */
public class VehiclePullout {

	private String vehicleId;
	private String agencyIdTcip;
	private String agencyId;
	private String depot;
	private String serviceDate;
	private String pulloutTime;
	private String run;
	private String operatorId;
	private String pullinTime;
	
	public String toString() {
	  return "VehiclePullout[vehicleId=" + vehicleId + ", run=" + run + "]";
	}
	/**
	 * @return the vehicleId
	 */
	@JsonProperty("vehicle-id")
	public String getVehicleId() {
		return vehicleId;
	}
	/**
	 * @param vehicleId the vehicleId to set
	 */
	public void setVehicleId(String vehicleId) {
		this.vehicleId = vehicleId;
	}
	/**
	 * @return the agencyIdTcip
	 */
	@JsonProperty("agency-id-tcip")
	public String getAgencyIdTcip() {
		return agencyIdTcip;
	}
	/**
	 * @param agencyIdTcip the agencyIdTcip to set
	 */
	public void setAgencyIdTcip(String agencyIdTcip) {
		this.agencyIdTcip = agencyIdTcip;
	}
	/**
	 * @return the agencyId
	 */
	@JsonProperty("agency-id")
	public String getAgencyId() {
		return agencyId;
	}
	/**
	 * @param agencyId the agencyId to set
	 */
	public void setAgencyId(String agencyId) {
		this.agencyId = agencyId;
	}
	/**
	 * @return the depot
	 */
	@JsonProperty("depot")
	public String getDepot() {
		return depot;
	}
	/**
	 * @param depot the depot to set
	 */
	public void setDepot(String depot) {
		this.depot = depot;
	}
	/**
	 * @return the serviceDate
	 */
	@JsonProperty("service-date")
	public String getServiceDate() {
		return serviceDate;
	}
	/**
	 * @param serviceDate the serviceDate to set
	 */
	public void setServiceDate(String serviceDate) {
		this.serviceDate = serviceDate;
	}
	/**
	 * @return the pulloutTime
	 */
	@JsonProperty("pullout-time")
	public String getPulloutTime() {
		return pulloutTime;
	}
	/**
	 * @param pulloutTime the pulloutTime to set
	 */
	public void setPulloutTime(String pulloutTime) {
		this.pulloutTime = pulloutTime;
	}
	/**
	 * @return the run
	 */
	@JsonProperty("run")
	public String getRun() {
		return run;
	}
	/**
	 * @param run the run to set
	 */
	public void setRun(String run) {
		this.run = run;
	}
	/**
	 * @return the operatorId
	 */
	@JsonProperty("operator-id")
	public String getOperatorId() {
		return operatorId;
	}
	/**
	 * @param operatorId the operatorId to set
	 */
	public void setOperatorId(String operatorId) {
		this.operatorId = operatorId;
	}
	/**
	 * @return the pullinTime
	 */
	@JsonProperty("pullin-time")
	public String getPullinTime() {
		return pullinTime;
	}
	/**
	 * @param pullinTime the pullinTime to set
	 */
	public void setPullinTime(String pullinTime) {
		this.pullinTime = pullinTime;
	}
	
}
