package org.onebusaway.users.services.validation;

public interface SecretSource {
  public String getSecretForId(String id);
}
