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
package org.asteriskjava.fastagi.internal;

import org.asteriskjava.fastagi.AgiClientChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.command.AgiReplyCommand;
import org.asteriskjava.fastagi.reply.AgiReply;

public class AgiClientChannelImpl implements AgiClientChannel {

  private final AgiWriter agiWriter;

  public AgiClientChannelImpl(AgiWriter agiWriter) {
    this.agiWriter = agiWriter;
  }

  @Override
  public void sendDigit(char digit) throws AgiException {
    sendResponse(AgiReply.SC_SUCCESS, digit);
  }

  @Override
  public void sendResponse(int statusCode, int result) throws AgiException {
    AgiReplyCommand command = new AgiReplyCommand(statusCode, result);
    agiWriter.sendCommand(command);
  }

}
