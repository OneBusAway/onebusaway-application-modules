package org.onebusaway.users.services.validation;

import java.util.Map;

public interface KeyValidationService {
  
  public String generateKey(String input);

  public String generateKey(String providerId, String input);

  public boolean isValidKey(String key);
  
  public Map<String,String> getKeyInfo(String key);
}
