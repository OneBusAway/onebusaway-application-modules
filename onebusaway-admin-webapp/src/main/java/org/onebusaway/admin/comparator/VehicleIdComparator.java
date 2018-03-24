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
package org.onebusaway.admin.comparator;

import java.util.Comparator;

import org.apache.commons.lang.StringUtils;
import org.onebusaway.admin.model.ui.VehicleStatus;

/**
 * Compares vehicle records by vehicle ids nulls last
 * @author abelsare
 *
 */
public class VehicleIdComparator implements Comparator<VehicleStatus>{

	private String order;
	
	public VehicleIdComparator(String order) {
		this.order = order;
	}
	
	@Override
	public int compare(VehicleStatus o1, VehicleStatus o2) {
		if(StringUtils.isBlank(o1.getVehicleId())) {
			return 1;
		}
		if(StringUtils.isBlank(o2.getVehicleId())) {
			return -1;
		}
		if(order.equalsIgnoreCase("desc")) {
			return new Integer(o2.getVehicleId()).compareTo(new Integer(o1.getVehicleId()));
		}
		return new Integer(o1.getVehicleId()).compareTo(new Integer(o2.getVehicleId()));
	}

}
