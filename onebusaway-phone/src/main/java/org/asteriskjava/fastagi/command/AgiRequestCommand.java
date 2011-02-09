package org.asteriskjava.fastagi.command;

import java.util.Map;
import java.util.TreeMap;

public class AgiRequestCommand extends AbstractAgiCommand {

  private static final long serialVersionUID = 1L;

  private Map<String, String> _parameters = new TreeMap<String, String>();
  
  public Map<String,String> getParameters() {
    return _parameters;
  }

  @Override
  public String buildCommand() {
    StringBuilder b = new StringBuilder();
    for (Map.Entry<String, String> entry : _parameters.entrySet()) {
      b.append(entry.getKey());
      b.append(": ");
      b.append(entry.getValue());
      b.append("\n");
    }
    return b.toString();
  }
}
