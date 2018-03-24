/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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
package org.onebusaway.enterprise.webapp.actions.api;

import java.util.Arrays;
import java.util.List;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletException;

import org.apache.struts2.ServletActionContext;
import org.onebusaway.enterprise.webapp.actions.OneBusAwayEnterpriseActionSupport;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public class WmataPingAction extends OneBusAwayEnterpriseActionSupport {

	private static final long serialVersionUID = 1L;
	
	private static Logger _log = LoggerFactory.getLogger(WmataPingAction.class);
	 
	@Autowired
	private TransitDataService _transitDataService;

	@Autowired
	private ConfigurationService _config;

	@Override
	public String execute() throws Exception {
		String pingStatus = null;
		try {
			pingStatus = getPing();
			ServletActionContext.getResponse().setContentType("text/plain");
			ServletActionContext.getResponse().getWriter().write(pingStatus);
		} catch (Throwable t) {
			ServletActionContext.getResponse().setStatus(500);
		}
		return null;
	}

	public String getPing() throws RuntimeException {
		try {
			List<AgencyWithCoverageBean> count = _transitDataService.getAgenciesWithCoverage();
			if (count == null || count.isEmpty()) {
				_log.error("Ping action found agencies = " + count);
				throw new ServletException("No agencies supported in current bundle");
			}
			
			String nextbusUrl = _config.getConfigurationValueAsString("nextbus.pingUrl",
					"http://localhost:8080/onebusaway-nextbus-api-webapp/service/publicXMLFeed?command=agencyList");
			
			String smsUrl = _config.getConfigurationValueAsString("sms.pingUrl",
					"http://localhost:8080/onebusaway-sms-webapp/");
			
			String apiUrl = _config.getConfigurationValueAsString("api.pingUrl",
					"http://localhost:8080/onebusaway-api-webapp/api/where/config.json?key=OBA");
			
			for (String url : Arrays.asList(nextbusUrl, smsUrl, apiUrl))
				if (!isSuccessful(url))
					throw new ServletException("Url " + url + " is not responding");

			return "size: " + count.size();

		} catch (Throwable t) {
			_log.error("Ping action failed with ", t);
			throw new RuntimeException(t);
		}
	}

	private boolean isSuccessful(String url) {
		try {
			HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
			conn.connect();
			return (conn.getResponseCode() == 200);
		} catch (Exception e) {
			return false;
		}
	}

}
