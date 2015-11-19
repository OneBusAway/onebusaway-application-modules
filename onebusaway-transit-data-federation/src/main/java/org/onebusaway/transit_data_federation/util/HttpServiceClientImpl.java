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
package org.onebusaway.transit_data_federation.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class HttpServiceClientImpl implements HttpServiceClient {
	
	private static Logger _log = LoggerFactory
			.getLogger(HttpServiceClientImpl.class);
	
	private String _hostname = null;

	private String _apiEndpointPath = "/api/";

	private int _port = 80;
	
	private RestApiLibrary _restApiLibrary;
	
	public HttpServiceClientImpl(String protocol, String hostname, Integer port, String path) {
		_hostname = hostname;
		if (port != null) {
			_port = port;
		}

		if (path != null) {
			_apiEndpointPath = path;
		}

		_log.info("Rest Service hostname = " + _hostname);

		if (!StringUtils.isBlank(_hostname))
			_restApiLibrary = new RestApiLibrary(protocol, _hostname, _port,
					_apiEndpointPath);
		else
			_log.warn("No Rest URL given!");
	}
	
	public HttpServiceClientImpl(String host, Integer port, String apiPrefix) {
		this("http", host, port, apiPrefix);
	}
	
	@Override
	public URL buildUrl(String baseObject, String... params) throws Exception {
		return _restApiLibrary.buildUrl(baseObject, params);
	}

	@Override
	public String log(String baseObject, String component, Integer priority,
			String message) {
		return _restApiLibrary.log(baseObject, component, priority, message);
	}

	@Override
	public List<JsonObject> getItemsForRequest(String baseObject,
			String... params) throws IOException{ 
		if (_restApiLibrary == null)
			return Collections.emptyList();
		URL requestUrl = _restApiLibrary.buildUrl(baseObject, params);
		_log.info("Requesting " + requestUrl);

		String responseJson = _restApiLibrary
				.getContentsOfUrlAsString(requestUrl);

		return _restApiLibrary.getJsonObjectsForString(responseJson);
	}

	@Override
	public List<Map<String, String>> getItems(String baseObject,
			String... params) throws IOException {
		if (_restApiLibrary == null)
			return Collections.emptyList();
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		List<JsonObject> items = getItemsForRequest(baseObject, params);
		for (JsonObject item : items) {
			Map<String, String> m = new HashMap<String, String>();
			result.add(m);
			for (Map.Entry<String, JsonElement> entry : item.entrySet()) {
				m.put(entry.getKey(), entry.getValue().getAsString());
			}
		}
		return result;
	}

	@Override
	public String getItem(String baseObject, String key) throws IOException {
		List<Map<String, String>> items = getItems("config", "list");
		if (items == null) return null;
		for (Map<String, String> component : items) {
			if (component.containsKey("key") && key.equals(component.get("key"))) {
				return component.get("value");
			}
		}
		return null;
	}

}
