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
 * Holds real time statistics for the vehicles fetched from operational api 
 * @author abelsare
 *
 */
public class VehicleStatistics {

	private int vehiclesInEmergency;
	private int vehiclesInRevenueService;
	private int vehiclesTracked;
	private int activeRuns;
	/**
	 * @return the vehiclesInEmergency
	 */
	public int getVehiclesInEmergency() {
		return vehiclesInEmergency;
	}
	/**
	 * @param vehiclesInEmergency the vehiclesInEmergency to set
	 */
	public void setVehiclesInEmergency(int vehiclesInEmergency) {
		this.vehiclesInEmergency = vehiclesInEmergency;
	}
	/**
	 * @return the vehiclesInRevenueService
	 */
	public int getVehiclesInRevenueService() {
		return vehiclesInRevenueService;
	}
	/**
	 * @param vehiclesInRevenueService the vehiclesInRevenueService to set
	 */
	public void setVehiclesInRevenueService(int vehiclesInRevenueService) {
		this.vehiclesInRevenueService = vehiclesInRevenueService;
	}
	/**
	 * @return the vehiclesTracked
	 */
	public int getVehiclesTracked() {
		return vehiclesTracked;
	}
	/**
	 * @param vehiclesTracked the vehiclesTracked to set
	 */
	public void setVehiclesTracked(int vehiclesTracked) {
		this.vehiclesTracked = vehiclesTracked;
	}
	/**
	 * @return the activeRuns
	 */
	public int getActiveRuns() {
		return activeRuns;
	}
	/**
	 * @param activeRuns the activeRuns to set
	 */
	public void setActiveRuns(int activeRuns) {
		this.activeRuns = activeRuns;
	}
	
}
