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
package org.onebusaway.api.impl;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.onebusaway.api.services.AlarmDetails;
import org.onebusaway.api.services.AlarmService;
import org.onebusaway.api.services.apns.ApplePushNotificationService;
import org.onebusaway.exceptions.InvalidArgumentServiceException;
import org.onebusaway.transit_data.model.RegisterAlarmQueryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.notnoop.apns.PayloadBuilder;

@Component
class AlarmServiceImpl implements AlarmService {

  private static Logger _log = LoggerFactory.getLogger(AlarmServiceImpl.class);

  private static final String PREFIX_APNS = "apns:";

  private ConcurrentMap<String, AlarmDetails> _alarmsById = new ConcurrentHashMap<String, AlarmDetails>();

  private List<ApplePushNotificationService> _applePushNotificationServices = Collections.emptyList();

  private String _callbackUrl;

  @Autowired(required=false)
  public void setApplePushNotificationServices(
      List<ApplePushNotificationService> applePushNotificationServices) {
    _applePushNotificationServices = applePushNotificationServices;
  }

  public String getCallbackUrl() {
    return _callbackUrl;
  }

  public void setCallbackUrl(String callbackUrl) {
    _callbackUrl = callbackUrl;
  }

  @Override
  public AlarmDetails alterAlarmQuery(RegisterAlarmQueryBean alarm, String data) {

    String url = alarm.getUrl();

    if (url.startsWith(PREFIX_APNS)) {

      String deviceId = url.substring(PREFIX_APNS.length());

      /**
       * Update the callback url
       */
      String callbackUrl = getCallbackUrl();
      alarm.setUrl(callbackUrl);

      if (_applePushNotificationServices.isEmpty())
        _log.warn("apple push notification alarm set but not ApplePushNotificationService was configured");

      return new ApnsAlarmDetails(deviceId, data);
    }

    return null;
  }

  @Override
  public void registerAlarm(String alarmId, AlarmDetails alarmDetails) {
    if (alarmId == null)
      throw new IllegalArgumentException("alarmId is null");
    if (alarmDetails == null)
      throw new IllegalArgumentException("alarmDetails is null");
    _alarmsById.put(alarmId, alarmDetails);
  }

  @Override
  public void fireAlarm(String alarmId) {

    AlarmDetails details = _alarmsById.remove(alarmId);

    if (details == null)
      return;

    if (details instanceof ApnsAlarmDetails) {

      ApnsAlarmDetails apnsDetails = (ApnsAlarmDetails) details;

      JSONObject data = getDataAsJson(apnsDetails.getData());

      String payload = getDataAsApnsPayload(alarmId, data);
      boolean isProduction = isProduction(data);

      for (ApplePushNotificationService service : _applePushNotificationServices) {
        if (service.isProduction() == isProduction) {
          service.pushNotification(apnsDetails.getDeviceToken(), payload);
          return;
        }
      }

      _log.warn("no appropriate ApplePushNotificationService found for alarm: production="
          + isProduction);
    }
  }

  @Override
  public void cancelAlarm(String alarmId) {
    _alarmsById.remove(alarmId);
  }

  /****
   * Private Methods
   ****/

  private JSONObject getDataAsJson(String data) {
    if (data == null)
      return new JSONObject();
    try {
      return new JSONObject(data);
    } catch (JSONException e) {
      throw new InvalidArgumentServiceException("data", e.getMessage());
    }
  }

  private String getDataAsApnsPayload(String alarmId, JSONObject data) {

    PayloadBuilder b = PayloadBuilder.newPayload();

    b.customField("alarmId", alarmId);

    try {
      if (data.has("actionKey"))
        b.actionKey(data.getString("actionKey"));
      if (data.has("alertBody"))
        b.alertBody(data.getString("alertBody"));
      if (data.has("badge"))
        b.badge(data.getInt("badge"));
      if (data.has("sound"))
        b.sound(data.getString("sound"));
    } catch (JSONException e) {
      throw new InvalidArgumentServiceException("data", e.getMessage());
    }

    return b.build();
  }

  private boolean isProduction(JSONObject data) {
    try {
      if (data.has("production"))
        return data.getBoolean("production");
      return false;
    } catch (JSONException e) {
      throw new InvalidArgumentServiceException("data", e.getMessage());
    }
  }

  private static class ApnsAlarmDetails implements AlarmDetails {

    private final String _deviceToken;

    private final String _data;

    public ApnsAlarmDetails(String deviceToken, String data) {
      _deviceToken = deviceToken;
      _data = data;
    }

    public String getDeviceToken() {
      return _deviceToken;
    }

    public String getData() {
      return _data;
    }
  }
}
