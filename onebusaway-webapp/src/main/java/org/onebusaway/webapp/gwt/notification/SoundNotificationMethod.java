/**
 * 
 */
package org.onebusaway.webapp.gwt.notification;

import com.allen_sauer.gwt.voices.client.Sound;
import com.allen_sauer.gwt.voices.client.SoundController;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;

public class SoundNotificationMethod extends AbstractNotificationMethod {

  private Sound _soundResource;

  public SoundNotificationMethod() {
    super("sound", "Play a sound");

    DeferredCommand.addCommand(new Command() {

      @Override
      public void execute() {
        SoundController _soundController = new SoundController();
        _soundResource = _soundController.createSound(
            Sound.MIME_TYPE_AUDIO_X_WAV, " OneBusAwayNotificationSound.wav");
      }
    });

  }

  @Override
  protected void performNotification(NotificationContext context) {
    System.out.println("play sound! " + _soundResource.getLoadState());
    _soundResource.play();
  }
}