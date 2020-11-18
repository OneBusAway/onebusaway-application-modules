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
package org.onebusaway.nextbus.impl.rest.handler;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import com.opensymphony.xwork2.ActionInvocation;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.rest.handler.ContentTypeHandler;

/**
 * Handles JSON content using json-lib
 */
public class CustomJsonLibHandler implements ContentTypeHandler {

  public void toObject(Reader in, Object target) throws IOException {
    StringBuilder sb = new StringBuilder();
    char[] buffer = new char[1024];
    int len = 0;
    while ((len = in.read(buffer)) > 0) {
      sb.append(buffer, 0, len);
    }
    if (target != null && sb.length() > 0 && sb.charAt(0) == '[') {
      JSONArray jsonArray = JSONArray.fromObject(sb.toString());
      if (target.getClass().isArray()) {
        JSONArray.toArray(jsonArray, target, new JsonConfig());
      } else {
        JSONArray.toList(jsonArray, target, new JsonConfig());
      }

    } else {
      JSONObject jsonObject = JSONObject.fromObject(sb.toString());
      JSONObject.toBean(jsonObject, target, new JsonConfig());
    }
  }

  @Override
  public void toObject(ActionInvocation actionInvocation, Reader in, Object target) throws IOException {
    StringBuilder sb = new StringBuilder();
    char[] buffer = new char[1024];
    int len = 0;
    while ((len = in.read(buffer)) > 0) {
      sb.append(buffer, 0, len);
    }
    if (target != null && sb.length() > 0 && sb.charAt(0) == '[') {
      JSONArray jsonArray = JSONArray.fromObject(sb.toString());
      if (target.getClass().isArray()) {
        JSONArray.toArray(jsonArray, target, new JsonConfig());
      } else {
        JSONArray.toList(jsonArray, target, new JsonConfig());
      }

    } else {
      JSONObject jsonObject = JSONObject.fromObject(sb.toString());
      JSONObject.toBean(jsonObject, target, new JsonConfig());
    }

  }

  public String fromObject(Object obj, String resultCode, Writer stream)
      throws IOException {

    String callback = null;
    HttpServletRequest req = ServletActionContext.getRequest();
    if (req != null)
      callback = req.getParameter("callback");

    String value = null;

    if (obj != null) {
      if (isArray(obj)) {
        JSONArray jsonArray = JSONArray.fromObject(obj);
        value = jsonArray.toString();
      } else {
        JSONObject jsonObject = JSONObject.fromObject(obj);
        value = jsonObject.toString();
      }
    }

    if (value != null) {
      if (callback != null)
        stream.write(callback + "(" + value + ")");
      else
        stream.write(value);
    }
    
    return null;
  }

  @Override
  public String fromObject(ActionInvocation actionInvocation, Object obj, String s, Writer stream) throws IOException {

    String callback = null;
    HttpServletRequest req = ServletActionContext.getRequest();
    if (req != null)
      callback = req.getParameter("callback");

    String value = null;

    if (obj != null) {
      if (isArray(obj)) {
        JSONArray jsonArray = JSONArray.fromObject(obj);
        value = jsonArray.toString();
      } else {
        JSONObject jsonObject = JSONObject.fromObject(obj);
        value = jsonObject.toString();
      }
    }

    if (value != null) {
      if (callback != null)
        stream.write(callback + "(" + value + ")");
      else
        stream.write(value);
    }
    return null;
  }

  private boolean isArray(Object obj) {
    return obj instanceof Collection<?> || obj.getClass().isArray();
  }

  public String getContentType() {
    return "application/json";
  }

  public String getExtension() {
    return "json";
  }
}
