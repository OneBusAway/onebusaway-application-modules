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
package org.onebusaway.admin.util;

import java.util.List;

import org.onebusaway.admin.model.ui.VehicleStatus;

/**
 * Caches vehicle records fetched from TDM and operational API web services 
 * @author abelsare
 *
 */
public class VehicleStatusCache {
	
	private List<VehicleStatus> records;
	private List<VehicleStatus> searchResults;
	
	/**
	 * Add new records to the cache
	 * @param newRecords
	 */
	public void add(List<VehicleStatus> newRecords) {
		this.records = newRecords;
	}
	
	/**
	 * Retrieve the records present in the cache
	 * @return the records currently present in the cache
	 */
	public List<VehicleStatus> fetch() {
		return records;
	}
	
	/**
	 * Add search results to the cache
	 * @param newRecords
	 */
	public void addSearchResults(List<VehicleStatus> newResults) {
		this.searchResults = newResults;
	}
	
	/**
	 * Retrieve the search results present in the cache
	 * @return the records currently present in the cache
	 */
	public List<VehicleStatus> getSearchResults() {
		return searchResults;
	}

}
