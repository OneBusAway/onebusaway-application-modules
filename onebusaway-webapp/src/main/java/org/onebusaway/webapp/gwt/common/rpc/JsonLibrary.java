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
package org.onebusaway.webapp.gwt.common.rpc;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

public class JsonLibrary {

  public native static void getJson(String requestId, String url,
      JsonCallback handler, int timeout) /*-{
          
    var callback = "callback" + requestId;
    var callbackDone = callback + "done";
    
    var script = document.createElement("script");
    script.setAttribute("src", url+callback);
    script.setAttribute("type", "text/javascript");

    window[callback] = function(jsonObj) {
      window[callbackDone] = true;
      handler.@org.onebusaway.webapp.gwt.common.rpc.JsonCallback::onSuccess(Lcom/google/gwt/core/client/JavaScriptObject;)(jsonObj);
    }
    
    // JSON download has a timeout, after which we check that it is completed, throw an error if not, and then cleanup
    setTimeout(function() {
      if (!window[callbackDone]) {
        handler.@org.onebusaway.webapp.gwt.common.rpc.JsonCallback::onFailure()();
      } 

      // cleanup
      // Garbage collect the callbacks at minimum, delete if supported
      window[ callback ] = undefined;
      try{ delete window[ callback ]; } catch(e){}
      window[ callbackDone ] = undefined;
      try{ delete window[ callbackDone ]; } catch(e){}
      document.body.removeChild(script);
    }, timeout);
    
    document.body.appendChild(script);
   }-*/;

  public static Double getJsonDouble(JSONObject object, String key) {
    JSONValue value = object.get(key);
    if (value == null)
      return null;
    JSONNumber v = value.isNumber();
    if (v == null)
      return null;
    return new Double(v.doubleValue());
  }
  
  public static Boolean getJsonBoolean(JSONObject object, String key) {
    JSONValue value = object.get(key);
    if(value == null)
      return null;
    JSONBoolean b = value.isBoolean();
    if( b == null)
      return null;
    return new Boolean(b.booleanValue());
  }

  public static String getJsonString(JSONObject object, String key) {
    JSONValue value = object.get(key);
    if (value == null)
      return null;
    JSONString string = value.isString();
    if (string == null)
      return null;
    return string.stringValue();
  }

  public static JSONObject getJsonObj(JSONObject object, String key) {
    JSONValue value = object.get(key);
    if (value == null)
      return null;
    return value.isObject();
  }

  public static JSONArray getJsonArray(JSONObject object, String key) {
    JSONValue value = object.get(key);
    if (value == null)
      return null;
    return value.isArray();
  }
}
