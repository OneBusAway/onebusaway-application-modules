package org.onebusaway.users.services.validation;

import java.util.Map;

public interface KeyValidationProvider {
  
  public String getId();

  public String generateKey(String input);

  public boolean isValidKey(String key);

  public void getKeyInfo(String subKey, Map<String, String> info);
}
