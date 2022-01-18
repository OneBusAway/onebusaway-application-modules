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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Holds vehicle's last known information
 * @author abelsare
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VehicleLastKnownRecord {
	
	private String vehicleAgencyId;
    private String timeReported;
    private String timeReceived;
    private String archiveTimeReceived;
    private String operatorIdDesignator;
    private String routeIdDesignator;
    private String runIdDesignator;
    private String agencyId;
    private String vehicleId;
    private String depotId;
    private String serviceDate;
    private String destinationSignCode;
    private String inferredPhase;
    private String inferredStatus;
    private String inferredDSC;
    private String inferredDirectionId;
    private String inferredRunId;
    private String inferredRouteId;
    private String inferredTripId;
    private String emergencyCode;
    private Double latitude;
    private Double longitude;
    private Double direction;
    private Long scheduleDeviation;
    private boolean inferrenceFormal;
	
	/**
	 * @return the vehicleAgencyId
	 */
    @JsonProperty("vehicle-agency-id")
	public String getVehicleAgencyId() {
		return vehicleAgencyId;
	}
	/**
	 * @param vehicleAgencyId the vehicleAgencyId to set
	 */
	public void setVehicleAgencyId(String vehicleAgencyId) {
		this.vehicleAgencyId = vehicleAgencyId;
	}
	/**
	 * @return the timeReported
	 */
	 @JsonProperty("time-reported")
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
	 * @return the archiveTimeReceived
	 */
	 @JsonProperty("archive-time-received")
	public String getArchiveTimeReceived() {
		return archiveTimeReceived;
	}
	/**
	 * @param archiveTimeReceived the archiveTimeReceived to set
	 */
	public void setArchiveTimeReceived(String archiveTimeReceived) {
		this.archiveTimeReceived = archiveTimeReceived;
	}
	/**
	 * @return the operatorIdDesignator
	 */
	 @JsonProperty("operator-id-designator")
	public String getOperatorIdDesignator() {
		return operatorIdDesignator;
	}
	/**
	 * @param operatorIdDesignator the operatorIdDesignator to set
	 */
	public void setOperatorIdDesignator(String operatorIdDesignator) {
		this.operatorIdDesignator = operatorIdDesignator;
	}
	/**
	 * @return the routeIdDesignator
	 */
	 @JsonProperty("route-id-designator")
	public String getRouteIdDesignator() {
		return routeIdDesignator;
	}
	/**
	 * @param routeIdDesignator the routeIdDesignator to set
	 */
	public void setRouteIdDesignator(String routeIdDesignator) {
		this.routeIdDesignator = routeIdDesignator;
	}
	/**
	 * @return the runIdDesignator
	 */
	 @JsonProperty("run-id-designator")
	public String getRunIdDesignator() {
		return runIdDesignator;
	}
	/**
	 * @param runIdDesignator the runIdDesignator to set
	 */
	public void setRunIdDesignator(String runIdDesignator) {
		this.runIdDesignator = runIdDesignator;
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
	 * @return the depotId
	 */
	@JsonProperty("depot-id")
	public String getDepotId() {
		return depotId;
	}
	/**
	 * @param depotId the depotId to set
	 */
	public void setDepotId(String depotId) {
		this.depotId = depotId;
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
	 * @return the inferredPhase
	 */
	@JsonProperty("inferred-phase")
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
	 * @return the inferredStatus
	 */
	@JsonProperty("inferred-status")
	public String getInferredStatus() {
		return inferredStatus;
	}
	/**
	 * @param inferredStatus the inferredStatus to set
	 */
	public void setInferredStatus(String inferredStatus) {
		this.inferredStatus = inferredStatus;
	}
	/**
	 * @return the inferredDSC
	 */
	@JsonProperty("inferred-dest-sign-code")
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
	 * @return the inferredDirectionId
	 */
	@JsonProperty("inferred-direction-id")
	public String getInferredDirectionId() {
		return inferredDirectionId;
	}
	/**
	 * @param inferredDirectionId the inferredDirectionId to set
	 */
	public void setInferredDirectionId(String inferredDirectionId) {
		this.inferredDirectionId = inferredDirectionId;
	}
	/**
	 * @return the inferredRunId
	 */
	@JsonProperty("inferred-run-id")
	public String getInferredRunId() {
		return inferredRunId;
	}
	/**
	 * @param inferredRunId the inferredRunId to set
	 */
	public void setInferredRunId(String inferredRunId) {
		this.inferredRunId = inferredRunId;
	}
	@JsonProperty("inferred-trip-id")
	public String getInferredTripId() {
		return inferredTripId;
	}
	public void setInferredTripId(String inferredTripId) {
		this.inferredTripId = inferredTripId;
	}
	/**
	 * @return the destinationSignCode
	 */
	@JsonProperty("dest-sign-code")
	public String getDestinationSignCode() {
		return destinationSignCode;
	}
	/**
	 * @param destinationSignCode the destinationSignCode to set
	 */
	public void setDestinationSignCode(String destinationSignCode) {
		this.destinationSignCode = destinationSignCode;
	}
	/**
	 * @return the emergencyCode
	 */
	 @JsonProperty("emergency-code")
	public String getEmergencyCode() {
		return emergencyCode;
	}
	/**
	 * @param emergencyCode the emergencyCode to set
	 */
	public void setEmergencyCode(String emergencyCode) {
		this.emergencyCode = emergencyCode;
	}

	@JsonProperty("latitude")
	public Double getLatitude() {
		return latitude;
	}
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
	@JsonProperty("longitude")
	public Double getLongitude() {
		return longitude;
	}
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
	@JsonProperty("direction-deg")
	public Double getDirection() {
		return direction;
	}
	public void setDirection(Double direction) {
		this.direction = direction;
	}
	/**
	 * @return the timeReceived
	 */
	@JsonProperty("time-received")
	public String getTimeReceived() {
		return timeReceived;
	}
	/**
	 * @param timeReceived the timeReceived to set
	 */
	public void setTimeReceived(String timeReceived) {
		this.timeReceived = timeReceived;
	}
	
	@JsonProperty("schedule-deviation")
	public Long getScheduleDeviation() {
		return scheduleDeviation;
	}
	public void setScheduleDeviation(Long scheduleDeviation) {
		this.scheduleDeviation = scheduleDeviation;
	}
	/**
	 * @return the inferredRouteId
	 */
	@JsonProperty("inferred-route-id")
	public String getInferredRouteId() {
		return inferredRouteId;
	}
	/**
	 * @param inferredRouteId the inferredRouteId to set
	 */
	public void setInferredRouteId(String inferredRouteId) {
		this.inferredRouteId = inferredRouteId;
	}
	/**
	 * @return the inferrenceFormal
	 */
	@JsonProperty("inference-is-formal")
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
