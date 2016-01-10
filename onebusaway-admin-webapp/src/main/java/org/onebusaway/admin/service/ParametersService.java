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
package org.onebusaway.admin.service;

import java.util.Map;


/**
 * Saves admin parameter values on TDM server. Uses {@link ConfigurationService} to persist new 
 * parameter values on TDM.
 * @author abelsare
 *
 */
public interface ParametersService {

	/**
	 * Saves given parameter values to TDM server. 
	 * @param paramters parameters to save
	 * @return true if all parameters are saved sucessfully
	 */
	boolean saveParameters(Map<String, String> parameters);
	
	/**
	 * Returns all key value pairs of configuration stored on TDM server
	 * @return collection of configuration key value pairs
	 */
	Map<String, String> getParameters();
}
