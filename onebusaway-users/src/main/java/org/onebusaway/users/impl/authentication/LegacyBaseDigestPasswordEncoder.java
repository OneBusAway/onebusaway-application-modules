/**
 * Copyright (C) 2021 Cambridge Systematics, Inc.
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

import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Legacy support for Digest Passwords encoding. Not considered secure.
 */
public abstract class LegacyBaseDigestPasswordEncoder implements PasswordEncoder {
  private boolean encodeHashAsBase64 = false;

  public boolean getEncodeHashAsBase64() {
    return this.encodeHashAsBase64;
  }

  public void setEncodeHashAsBase64(boolean encodeHashAsBase64) {
    this.encodeHashAsBase64 = encodeHashAsBase64;
  }

}
