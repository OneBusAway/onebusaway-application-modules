package org.onebusaway.api.services.apns;

public interface ApplePushNotificationService {

  public void pushNotification(String deviceToken, String payload);

}
