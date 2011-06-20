package org.onebusaway.sms.services;

import java.util.Map;

public interface SessionManager {

  public Map<String, Object> getContext(String sessionId);

}
