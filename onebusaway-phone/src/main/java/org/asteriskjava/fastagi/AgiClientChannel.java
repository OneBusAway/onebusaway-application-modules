package org.asteriskjava.fastagi;

public interface AgiClientChannel {
  public void sendResponse(int statusCode, int result) throws AgiException;

  public void sendDigit(char digit) throws AgiException;
}
