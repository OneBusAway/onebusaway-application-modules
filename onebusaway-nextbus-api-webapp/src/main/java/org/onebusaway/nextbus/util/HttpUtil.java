package org.onebusaway.nextbus.util;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import com.google.gson.JsonObject;

public interface HttpUtil {
	JsonObject getJsonObject(String uri, int timeout) throws ClientProtocolException, IOException;
	String getEncodedUrl(String url);
}
