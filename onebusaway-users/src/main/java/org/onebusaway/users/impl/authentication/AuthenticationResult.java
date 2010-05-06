package org.onebusaway.users.impl.authentication;

public class AuthenticationResult {

  public enum EResultCode {
    NO_SUCH_PROVIDER, AUTHENTICATION_FAILED, SUCCESS
  };

  private final EResultCode code;

  private final String provider;

  private final String identity;

  public AuthenticationResult(EResultCode code) {
    this(code, null, null);
  }

  public AuthenticationResult(EResultCode code, String provider) {
    this(code, provider, null);
  }

  public AuthenticationResult(EResultCode code, String provider, String identity) {
    this.code = code;
    this.provider = provider;
    this.identity = identity;
  }
  
  public EResultCode getCode() {
    return code;
  }

  public boolean isAuthenticated() {
    return code == EResultCode.SUCCESS;
  }

  public String getProvider() {
    return provider;
  }

  public String getIdentity() {
    return identity;
  }
}
