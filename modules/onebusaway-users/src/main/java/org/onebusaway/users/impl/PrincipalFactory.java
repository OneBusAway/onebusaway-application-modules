package org.onebusaway.users.impl;

import java.util.UUID;

public class PrincipalFactory {
  public static String createPrincipal() {
    UUID uuid = UUID.randomUUID();
    return "v1_" + uuid.toString();
  }
}
