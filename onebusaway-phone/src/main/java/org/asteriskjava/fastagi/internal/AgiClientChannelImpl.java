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
