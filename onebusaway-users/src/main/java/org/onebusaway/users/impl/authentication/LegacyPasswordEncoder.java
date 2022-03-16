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

/**
 * Support legacy style salting of passwords.  This is not considered secure,
 * it is only present for backwards compatibility.
 */
public class LegacyPasswordEncoder extends LegacyMessageDigestPasswordEncoder{
  public LegacyPasswordEncoder(String algorithm) {
    super(algorithm);
  }

  public LegacyPasswordEncoder(String algorithm, boolean encodeHashAsBase64) throws IllegalArgumentException {
    super(algorithm, encodeHashAsBase64);
  }

  public String encodePassword(String user, String salt) {
    return encode("{" + user + "}" + salt);
  }
}
