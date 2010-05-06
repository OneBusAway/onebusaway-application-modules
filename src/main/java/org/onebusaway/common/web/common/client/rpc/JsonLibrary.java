package org.onebusaway.common.web.common.client.rpc;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

public class JsonLibrary {

  public native static void getJson(String requestId, String url,
      JsonCallback handler, int timeout) /*-{
    var callback = "callback" + requestId;
    
    var script = document.createElement("script");
    script.setAttribute("src", url+callback);
    script.setAttribute("type", "text/javascript");

    window[callback] = function(jsonObj) {
      window[callback + "done"] = true;
      handler.@org.onebusaway.common.web.common.client.rpc.JsonCallback::onSuccess(Lcom/google/gwt/core/client/JavaScriptObject;)(jsonObj);
    }
    
    // JSON download has 1-second timeout
    setTimeout(function() {
      if (!window[callback + "done"]) {
        handler.@org.onebusaway.common.web.common.client.rpc.JsonCallback::onFailure()();
      } 

      // cleanup
      document.body.removeChild(script);
      delete window[callback];
      delete window[callback + "done"];
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
