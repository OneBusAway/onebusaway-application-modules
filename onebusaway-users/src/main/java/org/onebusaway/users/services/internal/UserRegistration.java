package org.onebusaway.users.services.internal;

import java.io.Serializable;

public class UserRegistration implements Serializable {

  private static final long serialVersionUID = 1L;

  private final int userId;

  private final String registrationCode;

  public UserRegistration(int userId, String registrationCode) {
    this.userId = userId;
    this.registrationCode = registrationCode;
  }

  public int getUserId() {
    return userId;
  }

  public String getRegistrationCode() {
    return registrationCode;
  }
}
