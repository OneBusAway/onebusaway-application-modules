package org.onebusaway.users.impl;

import java.util.UUID;

import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.services.UserIndexTypes;

public class PrincipalFactory {
  public static UserIndexKey createPrincipal() {
    UUID uuid = UUID.randomUUID();
    return new UserIndexKey(UserIndexTypes.WEB,uuid.toString());
  }
}
