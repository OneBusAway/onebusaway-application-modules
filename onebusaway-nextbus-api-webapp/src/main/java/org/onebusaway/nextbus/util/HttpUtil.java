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
package org.onebusaway.nextbus.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import com.google.transit.realtime.GtfsRealtime;
import org.apache.http.client.ClientProtocolException;

import com.google.gson.JsonObject;

public interface HttpUtil {
	JsonObject getJsonObject(String uri, int timeout, Map<String, String> headersMap, Map<String, String> paramsMap) throws ClientProtocolException, IOException, URISyntaxException;
	GtfsRealtime.FeedMessage getFeedMessage(final String urlString,
											int timeoutSeconds,
											Map<String, String> headersMap,
											Map<String, String> paramsMap) throws ClientProtocolException, IOException, URISyntaxException;
	String getEncodedUrl(String url);
}
