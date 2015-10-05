/**
 * Copyright (C) 2015 Cambridge Systematics
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
package org.onebusaway.nextbus.actions.api;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.nextbus.model.nextbus.Body;
import org.onebusaway.nextbus.model.transiTime.Predictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.opensymphony.xwork2.ModelDriven;

public class ScheduleHorizAction extends NextBusApiBase implements
		ModelDriven<Body<List<Predictions>>> {

	private static Logger _log = LoggerFactory
			.getLogger(ScheduleHorizAction.class);

	private String agencyId = "";

	private String stopId = "";

	private String routeId = "";

	public String getA() {
		return agencyId;
	}

	public void setA(String agencyId) {
		this.agencyId = getMappedAgency(agencyId);
	}

	public String getR() {
		return routeId;
	}

	public void setRouteTag(String routeId) {
		this.routeId = _routeCacheService.getRouteShortNameFromId(routeId);
	}

	public DefaultHttpHeaders index() {
		return new DefaultHttpHeaders("success");
	}

	public Body<List<Predictions>> getModel() {

		Body<List<Predictions>> body = new Body<List<Predictions>>();
		List<AgencyAndId> stopIds = new ArrayList<AgencyAndId>();

		if (isValid(body, stopIds)) {

			String serviceUrl = getServiceUrl() + agencyId + SCHEDULE_COMMAND
					+ "?";
			String route = "r=" + routeId;
			String uri = serviceUrl + route + "&format=" + REQUEST_TYPE;

			try {
				JsonArray scheduleJson = getJsonObject(uri).getAsJsonArray(
						"scheduleHorizStops");
				Type listType = new TypeToken<List<Predictions>>() {
				}.getType();
				List<List<Predictions>> schedules = new Gson().fromJson(
						scheduleJson, listType);
				
				

				body.getResponse().addAll(schedules);
			} catch (Exception e) {
				_log.error(e.getMessage());
			}
		}

		return body;

	}

	private boolean isValid(Body body, List<AgencyAndId> routeIds) {
		if (!isValidAgency(body, agencyId))
			return false;

		List<String> agencies = new ArrayList<String>();
		agencies.add(agencyId);

		if (!processRouteIds(routeId, routeIds, agencies, body))
			return false;

		return true;
	}
}
