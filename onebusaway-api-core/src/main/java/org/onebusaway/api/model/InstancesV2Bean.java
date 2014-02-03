/**
 * Copyright (C) 2014 Kurt Raschke <kurt@kurtraschke.com>
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

package org.onebusaway.api.model;

import java.io.Serializable;
import java.util.List;

public class InstancesV2Bean implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<InstanceDetailsV2Bean> instanceDetails;

	public InstancesV2Bean() {

	}

	public List<InstanceDetailsV2Bean> getInstanceDetails() {
		return instanceDetails;
	}

	public void setInstanceDetails(List<InstanceDetailsV2Bean> instanceDetails) {
		this.instanceDetails = instanceDetails;
	}

}
