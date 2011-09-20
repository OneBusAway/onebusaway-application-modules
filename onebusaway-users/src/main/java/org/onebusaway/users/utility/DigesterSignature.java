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

import org.apache.commons.codec.binary.Base64;

import java.util.Arrays;

public class DigesterSignature {
  private static final String ALGORITHM = "SHA1";

  public static String generateKey(String salt, String input) {

    byte[] signature = Digester.digestValue(ALGORITHM,salt.getBytes(),input.getBytes());

    Base64 coder = new Base64();

    String encodedSig = new String(coder.encode(signature));
    String encodedValue = new String(coder.encode(input.getBytes()));

    return encodedSig + encodedValue;
  }

  public static boolean isValidKey(String salt, String key) {

    if (key.length() < 28)
      return false;

    Base64 coder = new Base64();

    String encodedSig = key.substring(0, 28);
    String encodedValue = key.substring(28);

    byte[] rawInput = coder.decode(encodedValue.getBytes());

    byte[] actualSignature = Digester.digestValue(ALGORITHM,salt.getBytes(),rawInput);
    byte[] expectedSignature = coder.decode(encodedSig.getBytes());

    return Arrays.equals(actualSignature, expectedSignature);
  }
  
  public static String getDecodedValue(String key) {
    
    Base64 coder = new Base64();

    String encodedValue = key.substring(28);

    byte[] rawInput = coder.decode(encodedValue.getBytes());
    return new String(rawInput);
  }
}
