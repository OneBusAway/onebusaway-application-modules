package org.onebusaway.integration.phone;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.asteriskjava.fastagi.AgiClientChannel;
import org.asteriskjava.fastagi.AgiClientScript;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.DefaultAgiClient;
import org.asteriskjava.fastagi.reply.AgiReply;

public class PhoneClient extends DefaultAgiClient {

  private long _defaultTimeout = 20000;

  private BlockingQueue<AgiReply> _repliesFromAgi = new ArrayBlockingQueue<AgiReply>(
      10);

  private BlockingQueue<Character> _repliesToAgi = new ArrayBlockingQueue<Character>(
      100);

  public PhoneClient() {
    setScript(new AgiClientScriptImpl());
  }

  public void setDefaultTimeout(long defaultTimeout) {
    _defaultTimeout = defaultTimeout;
  }

  public AgiReply getReply() {
    try {
      return _repliesFromAgi.poll(_defaultTimeout, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return null;
    }
  }

  public String getReplyAsText() {
    return getReplyAsText(true);
  }

  public String getReplyAsText(boolean includeDefaultResponse) {

    AgiReply reply = getReply();
    if (includeDefaultResponse)
      sendResponse("\0");

    if (reply == null)
      return null;
    List<String> lines = reply.getLines();
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < lines.size(); i++) {
      if (i > 0)
        b.append('\n');
      b.append(lines.get(i));
    }
    return b.toString();
  }

  public void sendResponse(String response) {
    for (int i = 0; i < response.length(); i++)
      _repliesToAgi.add(response.charAt(i));
  }
  
  public void sendDefaultResponse() {
    _repliesToAgi.add('\0');
  }

  /****
   * Private Methods
   ****/

  private class AgiClientScriptImpl implements AgiClientScript {

    @Override
    public void service(AgiReply reply, AgiClientChannel channel)
        throws AgiException {

      _repliesFromAgi.add(reply);

      try {
        Character response = _repliesToAgi.poll(_defaultTimeout,
            TimeUnit.MILLISECONDS);
        if (response == null)
          response = (char) 0;
        channel.sendDigit(response);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      }

    }
  }

}
