package org.onebusaway.api.services;

import java.util.Map;

public interface ApiKeyValidationService {
  public String generateKey(String input);

  public String generateKey(String providerId, String input);

  public boolean isValidKey(String key);
  
  public Map<String,String> getKeyInfo(String key);
}
