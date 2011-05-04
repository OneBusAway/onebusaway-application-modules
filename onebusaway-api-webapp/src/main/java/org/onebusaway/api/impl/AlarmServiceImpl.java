package org.onebusaway.api.impl;

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

  private ApplePushNotificationService _applePushNotificationService;

  private String _callbackUrl;

  @Autowired(required = false)
  public void setApns(ApplePushNotificationService applePushNotificationService) {
    _applePushNotificationService = applePushNotificationService;
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

      if (_applePushNotificationService == null)
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

      if (_applePushNotificationService == null) {
        _log.warn("apple push notification alarm fired but not ApplePushNotificationService was configured");
        return;
      }

      String payload = getDataAsApnsPayload(alarmId, apnsDetails.getData());

      _applePushNotificationService.pushNotification(
          apnsDetails.getDeviceToken(), payload);
    }
  }

  @Override
  public void cancelAlarm(String alarmId) {
    _alarmsById.remove(alarmId);
  }

  /****
   * Private Methods
   ****/

  private String getDataAsApnsPayload(String alarmId, String data) {

    PayloadBuilder b = PayloadBuilder.newPayload();

    b.customField("alarmId", alarmId);

    if (data == null)
      return b.build();

    try {
      JSONObject json = new JSONObject(data);
      if (json.has("actionKey"))
        b.actionKey(json.getString("actionKey"));
      if (json.has("alertBody"))
        b.alertBody(json.getString("alertBody"));
      if (json.has("badge"))
        b.badge(json.getInt("badge"));
      if (json.has("sound"))
        b.sound(json.getString("sound"));
    } catch (JSONException e) {
      throw new InvalidArgumentServiceException("data", e.getMessage());
    }

    return b.build();
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
