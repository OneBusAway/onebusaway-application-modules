package org.onebusaway.users.impl.validation;

import java.util.HashMap;
import java.util.Map;

import org.onebusaway.users.services.validation.SecretSource;

public class SecretSourceImpl implements SecretSource {

  private Map<String, String> _secrets = new HashMap<String, String>();

  public void putSecrets(Map<String, String> secrets) {
    _secrets.putAll(secrets);
  }

  @Override
  public String getSecretForId(String id) {
    return _secrets.get(id);
  }
}
