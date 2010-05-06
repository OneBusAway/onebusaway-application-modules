package org.onebusaway.api.services;

import java.util.Map;

public interface ApiKeyValidationProvider {
  public String getId();

  public String generateKey(String input);

  public boolean isValidKey(String key);

  public void getKeyInfo(String subKey, Map<String, String> info);
}
