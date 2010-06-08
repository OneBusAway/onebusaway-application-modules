package org.onebusaway.users.impl.authentication;

import org.springframework.dao.DataAccessException;
import org.springframework.security.providers.encoding.PasswordEncoder;

public class VersionedPasswordEncoder implements PasswordEncoder {

  private static final String DEFAULT_SEPARATOR_CHARACTER = "|";

  private String _versionId;

  private PasswordEncoder _passwordEncoder;

  private String _separatorCharacter = DEFAULT_SEPARATOR_CHARACTER;

  public void setVersionId(String versionId) {
    _versionId = versionId;
  }
  
  public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
    _passwordEncoder = passwordEncoder;
  }

  @Override
  public String encodePassword(String rawPass, Object salt)
      throws DataAccessException {
    return _versionId + _separatorCharacter
        + _passwordEncoder.encodePassword(rawPass, salt);
  }

  @Override
  public boolean isPasswordValid(String encPass, String rawPass, Object salt)
      throws DataAccessException {

    int index = encPass.indexOf(_separatorCharacter);
    if (index == -1)
      return false;
    encPass = encPass.substring(index + 1);
    return _passwordEncoder.isPasswordValid(encPass, rawPass, salt);
  }
}
