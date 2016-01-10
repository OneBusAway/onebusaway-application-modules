/**
 * Copyright (C) 2011 Metropolitan Transportation Authority
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
package org.onebusaway.transit_data_federation.bundle.tasks.stif.model;

public class SignCodeRecord implements StifRecord {
	private String signCode;
	private String signRoute;
	private String signText;

	public void setSignCode(String signCode) {
		this.signCode = signCode;
	}

	public String getSignCode() {
		return signCode;
	}

	public void setSignRoute(String signRoute) {
		this.signRoute = signRoute;
	}

	public String getSignRoute() {
		return signRoute;
	}

	public void setSignText(String signText) {
		this.signText = signText;
	}

	public String getSignText() {
		return signText;
	}
}
