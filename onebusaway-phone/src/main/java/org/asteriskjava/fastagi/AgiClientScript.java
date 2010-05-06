package org.asteriskjava.fastagi;

import org.asteriskjava.fastagi.reply.AgiReply;

public interface AgiClientScript {
  public void service(AgiReply reply, AgiClientChannel channel) throws AgiException;
}
