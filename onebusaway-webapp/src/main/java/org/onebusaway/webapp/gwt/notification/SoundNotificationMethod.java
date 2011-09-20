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