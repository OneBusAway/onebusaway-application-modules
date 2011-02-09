package org.asteriskjava.fastagi.command;

import java.util.Map;
import java.util.TreeMap;

public class AgiReplyCommand extends AbstractAgiCommand {

  private static final long serialVersionUID = 1L;

  private Map<String, String> _parameters = new TreeMap<String, String>();

  private int _statusCode;

  private int _result;

  public AgiReplyCommand(int statusCode, int result) {
    _statusCode = statusCode;
    _result = result;
  }

  public Map<String, String> getParameters() {
    return _parameters;
  }

  @Override
  public String buildCommand() {
    StringBuilder b = new StringBuilder();
    b.append(_statusCode);
    b.append(" result=");
    b.append(_result);
    return b.toString();
  }
}
