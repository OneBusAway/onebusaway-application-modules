/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.util.git;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.onebusaway.util.git.GitRepositoryState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to load properties from git-commit-id plugin. 
 *
 */
public class GitRepositoryHelper {

	private static Logger _log = LoggerFactory.getLogger(GitRepositoryHelper.class);
	public Properties getProperties() 	    {
		Properties properties = new Properties();
		try {
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream("git.properties");
			if (inputStream != null) {
				properties.load(inputStream);
			} else {
				_log.error("git.properties file not found");
			}
		} catch (IOException ioe) {
		    _log.error("properties file not found:", ioe);
		}
		try {
		    return properties;
		} catch (Exception any) {
		    _log.error("exception creating properties:", any);
		    return null;
		}
	    }

	public GitRepositoryState getGitRepositoryState() {
		Properties properties = new Properties();
		try {
		    properties.load(getClass().getClassLoader().getResourceAsStream("git.properties"));
		} catch (IOException ioe) {
		    _log.error("properties file not found:", ioe);
		}
		try {
		    return new GitRepositoryState(properties);
		} catch (Exception any) {
		    _log.error("exception creating properties:", any);
		    return null;
		}
	}

}
