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
package org.onebusaway.admin.model;

import java.util.List;

import org.onebusaway.admin.model.ui.VehicleStatus;

/**
 * Holds the response object in the format expected by jqgrid on the client side
 * @author abelsare
 *
 */
public class VehicleGridResponse {
	private String page;
	private String total;
	private String records;
	private List<VehicleStatus> rows;
	
	public VehicleGridResponse() {
		
	}

	/**
	 * @return the page
	 */
	public String getPage() {
		return page;
	}

	/**
	 * @param page the page to set
	 */
	public void setPage(String page) {
		this.page = page;
	}

	/**
	 * @return the total
	 */
	public String getTotal() {
		return total;
	}

	/**
	 * @param total the total to set
	 */
	public void setTotal(String total) {
		this.total = total;
	}

	/**
	 * @return the records
	 */
	public String getRecords() {
		return records;
	}

	/**
	 * @param records the records to set
	 */
	public void setRecords(String records) {
		this.records = records;
	}

	/**
	 * @return the rows
	 */
	public List<VehicleStatus> getRows() {
		return rows;
	}

	/**
	 * @param rows the rows to set
	 */
	public void setRows(List<VehicleStatus> rows) {
		this.rows = rows;
	}
}
