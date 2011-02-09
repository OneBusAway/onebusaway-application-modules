package org.onebusaway.users.services.validation;

import java.util.Map;

public interface KeyValidationService {
  
  public String generateKeyWithDefaultProvider(String input, String... arguments);

  public String generateKey(String providerId, String input, String... arguments);

  public boolean isValidKey(String key, String... arguments);
  
  public Map<String,String> getKeyInfo(String key, String... arguments);
}
