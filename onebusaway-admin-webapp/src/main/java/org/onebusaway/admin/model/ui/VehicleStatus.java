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
package org.onebusaway.admin.model.ui;

/**
 * Holds vehicle status info
 * @author abelsare
 *
 */
public class VehicleStatus {
	
	private String status;
	private String vehicleId;
	private String lastUpdate;
	private String inferredPhase;
	private String inferredDSC;
	private String inferredDestination;
	private String observedDSC;
	private String pullinTime;
	private String pulloutTime;
	private String details;
	private String route;
	private String depot;
	private String emergencyStatus;
	private String formattedPulloutTime;
	private String formattedPullinTime;
	private String timeReported;
	private boolean inferrenceFormal;
	/**
	 * @return the vehicleId
	 */
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
	 * @return the lastUpdate
	 */
	public String getLastUpdate() {
		return lastUpdate;
	}
	/**
	 * @param lastUpdateTime the lastUpdate to set
	 */
	public void setLastUpdate(String lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	/**
	 * @return the inferredPhase
	 */
	public String getInferredPhase() {
		return inferredPhase;
	}
	/**
	 * @param inferredPhase the inferredPhase to set
	 */
	public void setInferredPhase(String inferredPhase) {
		this.inferredPhase = inferredPhase;
	}
	/**
	 * @return the observedDSC
	 */
	public String getObservedDSC() {
		return observedDSC;
	}
	/**
	 * @param observedDSC the observedDSC to set
	 */
	public void setObservedDSC(String observedDSC) {
		this.observedDSC = observedDSC;
	}
	/**
	 * @return the pullinTime
	 */
	public String getPullinTime() {
		return pullinTime;
	}
	/**
	 * @param pullinTime the pullinTime to set
	 */
	public void setPullinTime(String pullinTime) {
		this.pullinTime = pullinTime;
	}
	/**
	 * @return the pulloutTime
	 */
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
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	/**
	 * @return the inferredDestination
	 */
	public String getInferredDestination() {
		return inferredDestination;
	}
	/**
	 * @param inferredDestination the inferredDestination to set
	 */
	public void setInferredDestination(String inferredDestination) {
		this.inferredDestination = inferredDestination;
	}
	/**
	 * @return the details
	 */
	public String getDetails() {
		return details;
	}
	/**
	 * @param details the details to set
	 */
	public void setDetails(String details) {
		this.details = details;
	}
	/**
	 * @return the route
	 */
	public String getRoute() {
		return route;
	}
	/**
	 * @param route the route to set
	 */
	public void setRoute(String route) {
		this.route = route;
	}
	/**
	 * @return the depot
	 */
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
	 * @return the emergencyStatus
	 */
	public String getEmergencyStatus() {
		return emergencyStatus;
	}
	/**
	 * @param emergencyStatus the emergencyStatus to set
	 */
	public void setEmergencyStatus(String emergencyStatus) {
		this.emergencyStatus = emergencyStatus;
	}
	/**
	 * @return the formattedPulloutTime
	 */
	public String getFormattedPulloutTime() {
		return formattedPulloutTime;
	}
	/**
	 * @param formattedPulloutTime the formattedPulloutTime to set
	 */
	public void setFormattedPulloutTime(String formattedPulloutTime) {
		this.formattedPulloutTime = formattedPulloutTime;
	}
	/**
	 * @return the formattedPullinTime
	 */
	public String getFormattedPullinTime() {
		return formattedPullinTime;
	}
	/**
	 * @param formattedPullinTime the formattedPullinTime to set
	 */
	public void setFormattedPullinTime(String formattedPullinTime) {
		this.formattedPullinTime = formattedPullinTime;
	}
	/**
	 * @return the inferredDSC
	 */
	public String getInferredDSC() {
		return inferredDSC;
	}
	/**
	 * @param inferredDSC the inferredDSC to set
	 */
	public void setInferredDSC(String inferredDSC) {
		this.inferredDSC = inferredDSC;
	}
	/**
	 * @return the timeReported
	 */
	public String getTimeReported() {
		return timeReported;
	}
	/**
	 * @param timeReported the timeReported to set
	 */
	public void setTimeReported(String timeReported) {
		this.timeReported = timeReported;
	}
	/**
	 * @return the inferrenceFormal
	 */
	public boolean isInferrenceFormal() {
		return inferrenceFormal;
	}
	/**
	 * @param inferrenceFormal the inferrenceFormal to set
	 */
	public void setInferrenceFormal(boolean inferrenceFormal) {
		this.inferrenceFormal = inferrenceFormal;
	}
	
	

}
