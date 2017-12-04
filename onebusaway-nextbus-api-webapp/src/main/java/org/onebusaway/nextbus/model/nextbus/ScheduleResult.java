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
package org.onebusaway.nextbus.model.nextbus;

import java.util.Set;

import org.onebusaway.transit_data.model.trips.TripDetailsBean;


public class ScheduleResult {
	
	private Set<TripDetailsBean> tripDetails;
	
	private String directionId;
	
	private String blockId;

	public Set<TripDetailsBean> getTripDetails() {
		return tripDetails;
	}

	public void setTripDetails(Set<TripDetailsBean> tripDetails) {
		this.tripDetails = tripDetails;
	}

	public String getDirectionId() {
		return directionId;
	}

	public void setDirectionId(String directionId) {
		this.directionId = directionId;
	}

	public String getBlockId() {
		return blockId;
	}

	public void setBlockId(String blockId) {
		this.blockId = blockId;
	}
	
}
