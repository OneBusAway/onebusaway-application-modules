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
