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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class HttpUtil {

  public JsonObject getJsonObject(String uri, int timeoutSeconds) throws MalformedURLException,
      IOException {
    URL url = new URL(uri);
    HttpURLConnection request = (HttpURLConnection) url.openConnection();
    
    if (timeoutSeconds > 0) {
      request.setConnectTimeout(timeoutSeconds * 1000);
      request.setReadTimeout(timeoutSeconds * 1000);
    }
    request.connect();

    // Convert to a JSON object to print data
    JsonParser jp = new JsonParser(); // from gson
    JsonElement root = jp.parse(new InputStreamReader(
        (InputStream) request.getContent())); // Convert the input stream to a
    // json element
    JsonObject rootobj = root.getAsJsonObject(); // May be an array, may be
    // an
    // object.
    return rootobj;
  }
}
