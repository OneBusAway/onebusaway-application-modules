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
package org.onebusaway.webapp.gwt.notification;

import org.onebusaway.webapp.gwt.common.rpc.JsonLibrary;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Cookies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationStateDAO {

  private static final String COOKIE_NAME = "OBANotifications";

  private static final String DEFAULT_ID = "_";

  private static final int MAX_NODE_COUNT = 5;

  /****
   * JSON Keys
   ****/
  private static final String KEY_ID = "id";

  private static final String KEY_INDEX = "in";

  private static final String KEY_STATE = "st";

  private static final String KEY_MINUTES_BEFORE = "mb";

  private static final String KEY_METHODS = "ms";

  private static final String KEY_ENABLED = "en";

  /****
   * Public Methods
   ****/

  public void setState(String stopId, NotificationState state, boolean isDefault) {

    List<NotificationStateNode> nodes = loadState();

    NotificationStateNode match = null;

    for (NotificationStateNode node : nodes) {
      if ((isDefault && node.getStopId().equals(DEFAULT_ID)) || (!isDefault && node.getStopId().equals(stopId))) {
        match = node;
        break;
      }
    }

    if (match == null) {
      match = new NotificationStateNode();
      match.setStopId(isDefault ? DEFAULT_ID : stopId);
      nodes.add(match);
    }

    match.setState(state);

    reorderNodes(nodes, match);

    saveState(nodes);
  }

  public NotificationState getState(String stopId) {

    List<NotificationStateNode> nodes = loadState();
    NotificationStateNode defaultState = null;

    for (NotificationStateNode node : nodes) {
      if (stopId.equals(node.getStopId()))
        return node.getState();
      else if (DEFAULT_ID.equals(node.getStopId()))
        defaultState = node;
    }

    if (defaultState != null)
      return defaultState.getState();

    NotificationState state = new NotificationState();
    state.setMinutesBefore(5);
    return state;
  }

  /****
   * Private Methods
   ****/

  private void reorderNodes(List<NotificationStateNode> nodes, NotificationStateNode match) {

    int min = Integer.MAX_VALUE;
    for (NotificationStateNode node : nodes)
      min = Math.min(node.getIndex(), min);

    match.setIndex(min - 1);

    for (NotificationStateNode node : nodes) {
      if (node.getStopId().equals(DEFAULT_ID))
        node.setIndex(min - 2);
    }

    Collections.sort(nodes);

    while (nodes.size() > MAX_NODE_COUNT)
      nodes.remove(nodes.size() - 1);

    int index = 0;
    for (NotificationStateNode node : nodes)
      node.setIndex(index++);
  }

  private List<NotificationStateNode> loadState() {

    List<NotificationStateNode> nodes = new ArrayList<NotificationStateNode>();

    String cookieValue = Cookies.getCookie(COOKIE_NAME);

    if (cookieValue != null) {

      System.out.println("loading=" + cookieValue);

      JSONValue p = JSONParser.parse(cookieValue);
      JSONArray array = p.isArray();
      if (array != null) {
        for (int i = 0; i < array.size(); i++) {
          JSONValue entryValue = array.get(i);
          NotificationStateNode node = getJSONValueAsNode(entryValue);
          if (node != null)
            nodes.add(node);
        }
      }
    }

    return nodes;
  }

  private NotificationStateNode getJSONValueAsNode(JSONValue entryValue) {

    JSONObject object = entryValue.isObject();
    if (object == null)
      return null;

    NotificationStateNode node = new NotificationStateNode();

    String stopId = JsonLibrary.getJsonString(object, KEY_ID);
    if (stopId == null)
      return null;
    node.setStopId(stopId);

    Double index = JsonLibrary.getJsonDouble(object, KEY_INDEX);
    if (index == null)
      return null;
    node.setIndex(index.intValue());

    JSONObject stateObject = JsonLibrary.getJsonObj(object, KEY_STATE);
    if (stateObject == null)
      return null;
    NotificationState state = getJSONObjectAsState(stateObject);
    if (state == null)
      return null;
    node.setState(state);

    return node;
  }

  private NotificationState getJSONObjectAsState(JSONObject object) {

    NotificationState state = new NotificationState();

    Double minutesBefore = JsonLibrary.getJsonDouble(object, KEY_MINUTES_BEFORE);
    if (minutesBefore == null)
      return null;
    state.setMinutesBefore(minutesBefore.intValue());

    JSONArray jsonArray = JsonLibrary.getJsonArray(object, KEY_METHODS);
    if (jsonArray != null) {
      for (int i = 0; i < jsonArray.size(); i++) {
        JSONValue value = jsonArray.get(i);
        JSONObject methodObject = value.isObject();
        if (methodObject != null) {
          NotificationMethodState methodState = getJSONObjectAsMethodState(methodObject);
          if (methodState != null)
            state.getMethodStates().add(methodState);
        }
      }
    }

    return state;
  }

  private NotificationMethodState getJSONObjectAsMethodState(JSONObject object) {

    NotificationMethodState state = new NotificationMethodState();

    String id = JsonLibrary.getJsonString(object, KEY_ID);
    if (id == null)
      return null;
    state.setId(id);

    Boolean enabled = JsonLibrary.getJsonBoolean(object, KEY_ENABLED);
    if (enabled == null)
      return null;
    state.setEnabled(enabled.booleanValue());

    return state;
  }

  /****
   * 
   ****/

  private void saveState(List<NotificationStateNode> nodes) {
    JSONArray array = new JSONArray();
    int index = 0;
    for (NotificationStateNode node : nodes)
      array.set(index++, getNodeAsJSONObject(node));
    System.out.println("saving=" + array.toString());
    Cookies.setCookie(COOKIE_NAME, array.toString());
  }

  private JSONObject getNodeAsJSONObject(NotificationStateNode node) {
    JSONObject obj = new JSONObject();
    obj.put(KEY_ID, new JSONString(node.getStopId()));
    obj.put(KEY_INDEX, new JSONNumber(node.getIndex()));
    obj.put(KEY_STATE, getStateAsJSONObject(node.getState()));
    return obj;
  }

  private JSONObject getStateAsJSONObject(NotificationState state) {
    JSONObject obj = new JSONObject();
    obj.put(KEY_MINUTES_BEFORE, new JSONNumber(state.getMinutesBefore()));
    JSONArray methods = new JSONArray();
    int index = 0;
    for (NotificationMethodState methodState : state.getMethodStates()) {
      JSONObject methodObject = getMethodStateAsJSONObject(methodState);
      methods.set(index++, methodObject);
    }
    obj.put(KEY_METHODS, methods);
    return obj;
  }

  private JSONObject getMethodStateAsJSONObject(NotificationMethodState methodState) {
    JSONObject obj = new JSONObject();
    obj.put(KEY_ID, new JSONString(methodState.getId()));
    obj.put(KEY_ENABLED, JSONBoolean.getInstance(methodState.isEnabled()));
    return obj;
  }

}
