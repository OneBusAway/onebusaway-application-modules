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
package org.onebusaway.util.logging;

import org.apache.log4j.Level;

/**
 * Service interface to log system actions to a central server.
 * @author abelsare
 *
 */
public interface LoggingService {
	
	/**
	 * Logs the given message to central log location. Makes an API call to TDM which in turn persists
	 * this message in the database.
	 * @param component The chef component/role that initiated this action
	 * @param priority message priority/severity
	 * @param message the actual message to log
	 * @return response from remote server
	 */
	String log(String component, Level priority, String message);

}
