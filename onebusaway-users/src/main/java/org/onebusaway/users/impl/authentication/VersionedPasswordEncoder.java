/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.users.impl.authentication;

import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.encoding.PasswordEncoder;

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
