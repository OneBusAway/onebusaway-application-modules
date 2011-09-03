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
package org.onebusaway.users.utility;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Digester {

  public static byte[] digestValue(String algorithm, byte[] salt, byte[] input) {
    try {
      MessageDigest digest = MessageDigest.getInstance(algorithm);
      digest.update(salt);
      digest.update(input);
      return digest.digest();
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException(ex);
    }
  }
}
