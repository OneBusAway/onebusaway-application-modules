package org.onebusaway.api.services;

import org.onebusaway.users.services.ApiKeyPermissionService;

public class DummyApiKeyPermissionService implements ApiKeyPermissionService {

  @Override
  public boolean getPermission(String key, String service) {
    return true;
  }
  
}
