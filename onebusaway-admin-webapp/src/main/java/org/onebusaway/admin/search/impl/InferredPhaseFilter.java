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
package org.onebusaway.admin.search.impl;

import org.apache.commons.lang.StringUtils;
import org.onebusaway.admin.model.ui.VehicleStatus;
import org.onebusaway.admin.search.Filter;

/**
 * Filters vehicles by given inferred phase
 * @author abelsare
 *
 */
public class InferredPhaseFilter implements Filter<VehicleStatus> {
	
	private String inferredPhase;
	
	public InferredPhaseFilter(String inferredPhase) {
		this.inferredPhase = inferredPhase;
	}

	@Override
	public boolean apply(VehicleStatus type) {
		if(StringUtils.isNotBlank(type.getInferredPhase())) {
			return type.getInferredPhase().equalsIgnoreCase(inferredPhase);
		}
		return false;
	}

}
