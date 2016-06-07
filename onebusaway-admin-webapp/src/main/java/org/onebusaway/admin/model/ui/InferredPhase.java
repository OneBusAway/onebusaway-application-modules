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
 * Holds vehicle inferred phase values
 * @author abelsare
 *
 */
public enum InferredPhase {

	IN_PROGRESS("IN_PROGRESS"),
	DEADHEAD_BEFORE("DEADHEAD_BEFORE"),
	AT_BASE("AT_BASE"),
	DEADHEAD_AFTER("DEADHEAD_AFTER"),
	LAYOVER_BEFORE("LAYOVER_BEFORE"),
	DEADHEAD_DURING("DEADHEAD_DURING"),
	LAYOVER_DURING("LAYOVER_DURING");
	
	private String state;
	
	private InferredPhase(String state) {
		this.state = state;
	}
	
	public String getState() {
		return state;
	}
	
}
