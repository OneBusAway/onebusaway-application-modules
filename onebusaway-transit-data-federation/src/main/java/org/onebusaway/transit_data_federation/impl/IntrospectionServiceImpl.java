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

package org.onebusaway.transit_data_federation.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.onebusaway.container.ConfigurationParameter;
import org.onebusaway.transit_data.model.introspection.InstanceDetails;
import org.onebusaway.transit_data_federation.services.IntrospectionService;
import org.onebusaway.utility.GitRepositoryHelper;
import org.onebusaway.utility.GitRepositoryState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IntrospectionServiceImpl implements IntrospectionService {

	private static Logger _log = LoggerFactory
			.getLogger(IntrospectionServiceImpl.class);

	@Autowired(required = false)
	private InstanceDetails _instanceDetails;
	private String _instanceDetailsPropertyFile;

	private String getHostname() {
		String hostname = "unknown host";
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			_log.warn("Couldn't determine hostname", e);
		}

		return hostname;
	}
	
	/*@Autowired(required = false)*/
	private GitRepositoryState _gitRepositoryState;
	
	@Override
	public InstanceDetails getInstanceDetails() {
		return _instanceDetails;
	}

	@Override
	public GitRepositoryState getGitRepositoryState() {
		return _gitRepositoryState;
	}

	@ConfigurationParameter
	public void setInstanceDetailsPropertyFile(
			String instanceDetailsPropertyFile) {
		_instanceDetailsPropertyFile = instanceDetailsPropertyFile;
	}

	@PostConstruct
	public void start() {
		if(_gitRepositoryState == null){
			_gitRepositoryState = new GitRepositoryHelper().getGitRepositoryState();
		}
		
		if (_instanceDetails == null && _instanceDetailsPropertyFile != null) {
			InputStream in = null;
			try {
				Properties instanceDetails = new Properties();
				in = new FileInputStream(new File(_instanceDetailsPropertyFile));

				instanceDetails.load(in);

				_instanceDetails = new InstanceDetails(
						instanceDetails.getProperty("oba.instanceName"),
						instanceDetails.getProperty("oba.language"),
						instanceDetails.getProperty("oba.contactEmail"),
						instanceDetails.getProperty("oba.twitterUrl"),
						instanceDetails.getProperty("oba.facebookUrl"));

			} catch (Exception e) {
				_log.error(
						"Exception while processing instance details property file",
						e);
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						_log.warn("Exception while closing properties file", e);
					}
				}
			}
		} else if (_instanceDetails == null) {
			_instanceDetails = new InstanceDetails("OneBusAway on "
					+ getHostname(), Locale.getDefault().getLanguage(), "onebusaway@" + getHostname(),
					null, null);
		}

	}

}