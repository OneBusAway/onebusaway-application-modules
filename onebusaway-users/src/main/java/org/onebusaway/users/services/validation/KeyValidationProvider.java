package org.onebusaway.users.services.validation;

import java.util.Map;

public interface KeyValidationProvider {

  public String getId();

  public String generateKey(String input, String... arguments);

  public boolean isValidKey(String key, String... arguments);

  public void getKeyInfo(Map<String, String> info, String subKey,
      String... arguments);
}
