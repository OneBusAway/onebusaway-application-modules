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

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Component
public class HttpUtilImpl implements HttpUtil {

	private static Logger _log = LoggerFactory.getLogger(HttpUtilImpl.class);

	@Autowired
	private HttpClientPool _httpClientPool;

	public JsonObject getJsonObject(final String urlString, int timeoutSeconds, Map<String, String> headersMap,
									Map<String, String> paramsMap)
			throws ClientProtocolException, IOException, URISyntaxException {
		CloseableHttpResponse response = null;

		try {
			response = getResponse(urlString, timeoutSeconds, headersMap, paramsMap);
			HttpEntity entity = getEntity(response);
			InputStream content = entity.getContent();
			try{
				JsonParser jp = new JsonParser();
				JsonElement root = jp.parse(new InputStreamReader(rewriteStream(content)));
				JsonObject rootobj = root.getAsJsonObject();
				if (rootobj == null)
					return rootobj;
				if (rootobj.has("predictions"))
					return rootobj;
				if (rootobj.has("data")) {
					// alternative format, result is nested in wrapper
					rootobj = rootobj.get("data").getAsJsonObject();
					return rootobj;
				}
				return rootobj;
			} finally{
				content.close();
			}

		} catch (Exception e) {
			_log.error("Error handling url: " + urlString, e);
			throw e;
		} finally {
			response.close();
		}
	}

	private InputStream rewriteStream(InputStream content) throws IOException {
		if (content == null) return null;
		BufferedReader reader = new BufferedReader(new InputStreamReader(content));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(baos));
		String line = null;
		while ((line = reader.readLine()) != null) {
			// map destinations -> dest
			line = replaceToken(line, "destinations", "dest");
			// map directionId -> dir
			line = replaceToken(line, "directionId", "dir");
			// map predictions -> pred
			line = replaceToken(line, "predictions", "pred");
			// map tripId -> trip
			line = replaceToken(line, "tripId", "trip");
			// map vehicleId -> vehicle
			line = replaceToken(line, "vehicleId", "vehicle");
			// map predictionsData -> predictions
			line = replaceToken(line, "predictionsData", "predictions");
			writer.write(line);
		}
		writer.close();
		return new ByteArrayInputStream(baos.toByteArray());
	}

	private String replaceToken(String line, String token, String replacement) {
		String qToken = "\"" + token + "\"";
		String qReplacement = "\"" + replacement + "\"";
		return line.replace(qToken, qReplacement);
	}

	public FeedMessage getFeedMessage(final String urlString,
									  int timeoutSeconds,
									  Map<String, String> headersMap,
									  Map<String, String> paramsMap) throws ClientProtocolException, IOException, URISyntaxException {

		CloseableHttpResponse response = null;

		try {
			response = getResponse(urlString, timeoutSeconds, headersMap, paramsMap);
			HttpEntity entity = getEntity(response);
			InputStream content = entity.getContent();
			try{
				FeedMessage msg = FeedMessage.parseFrom(content);
				return msg;
			} finally{
				content.close();
			}

		} catch (Exception e) {
			_log.error("Error handling url: " + urlString, e);
			throw e;
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}

	public CloseableHttpResponse getResponse(String urlString, int timeoutSeconds, Map<String, String> headersMap,
											 Map<String, String> paramsMap) throws ClientProtocolException, IOException, URISyntaxException {

		URIBuilder uriBuilder = new URIBuilder(getEncodedUrl(urlString));

		for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
			uriBuilder.addParameter(entry.getKey(), entry.getValue());
		}

		URI uri = uriBuilder.build();

		HttpGet request = new HttpGet(uri);

		if (headersMap != null) {
			headersMap.forEach(request::setHeader);
		}

		RequestConfig.Builder config = RequestConfig.custom()
				.setConnectTimeout(timeoutSeconds * 1000)
				.setSocketTimeout(timeoutSeconds * 1000);
		request.setConfig(config.build());
		CloseableHttpClient httpClient = _httpClientPool.getClient();

		return httpClient.execute(request);
	}

	public HttpEntity getEntity(CloseableHttpResponse response) throws IOException{
		final String errorMessage;
		if (response != null) {
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					return entity;
				}
				else {
					errorMessage = "Failed to retrieve a valid HttpEntity object from the response";
				}
			} else {
				errorMessage = "HTTP Response: "
						+ response.getStatusLine().getStatusCode();
			}
		} else {
			errorMessage = "Received a Null response";
		}

		throw new IOException(errorMessage);
	}

	public String getEncodedUrl(String url) {
		List<NameValuePair> params = new LinkedList<NameValuePair>();
		;
		String[] urlParts = url.split("\\?");
		String encodedUrl;
		if (urlParts.length > 1) {
			String query = urlParts[1];
			for (String param : query.split("&")) {
				String[] pair = param.split("=");
				if (pair.length > 1) {
					params.add(new BasicNameValuePair(pair[0], pair[1]));
				} else {
					params.add(new BasicNameValuePair(pair[0],
							StringUtils.EMPTY));
				}
			}
			String paramString = URLEncodedUtils.format(params, "utf-8");
			encodedUrl = urlParts[0] + "?" + paramString;
		} else {
			encodedUrl = url;
		}

		return encodedUrl;
	}
}
