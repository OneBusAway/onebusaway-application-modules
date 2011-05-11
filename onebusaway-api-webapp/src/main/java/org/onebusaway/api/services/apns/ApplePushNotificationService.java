package org.onebusaway.api.services.apns;

public interface ApplePushNotificationService {
  
  public boolean isProduction();

  public void pushNotification(String deviceToken, String payload);

}
