/**
 * 
 */
package org.onebusaway.webapp.gwt.notification;

import com.google.gwt.libideas.resources.client.SoundResource;

public class SoundNotificationMethod extends AbstractNotificationMethod {

  private SoundResource _soundResource;

  public SoundNotificationMethod() {
    super("sound", "Play a sound");

    NotificationWidgetStandardResources resources = NotificationWidgetStandardResources.INSTANCE;
    _soundResource = resources.getNotificationSound();
  }

  @Override
  protected void performNotification(NotificationContext context) {
    _soundResource.play();
  }
}