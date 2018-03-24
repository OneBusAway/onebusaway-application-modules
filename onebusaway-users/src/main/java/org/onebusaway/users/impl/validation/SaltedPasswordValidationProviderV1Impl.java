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
package org.onebusaway.users.impl.validation;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;

import org.apache.commons.codec.binary.Base64;
import org.onebusaway.users.services.validation.KeyValidationProvider;
import org.onebusaway.users.utility.Digester;
import org.onebusaway.users.utility.DigesterSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This {@link KeyValidationProvider} is used to validate passwords. A password
 * is salted with a randomly-generated salt and then an SHA-256 digest of the
 * salt + password is generated as the key. This key is appropriate for
 * long-term storage, as the salt hopefully prevents against rainbow attacks.
 * 
 * The key can then be used to validate a password in the future. When
 * validating, the key is expected as the primary argument to
 * {@link #isValidKey(String, String...)}, while the password is expected as the
 * first optional argument.
 * 
 * @author bdferris
 * 
 */
public class SaltedPasswordValidationProviderV1Impl implements
    KeyValidationProvider {
  
  public static final String PROVIDER_ID = "spv1";

  private static Logger _log = LoggerFactory.getLogger(SaltedPasswordValidationProviderV1Impl.class);

  private static final String ALGORITHM = "SHA-256";

  private static final String KEY_DELIMITER = "|";

  private Base64 _coder = new Base64();

  public String getId() {
    return PROVIDER_ID;
  }

  public String generateKey(String input, String... arguments) {
    String salt = generateRandomSalt(10);
    byte[] signature = Digester.digestValue(ALGORITHM, salt.getBytes(),
        input.getBytes());
    String encodedSig = new String(_coder.encode(signature));
    return salt + KEY_DELIMITER + encodedSig;
  }

  public boolean isValidKey(String key, String... arguments) {

    if (arguments.length != 1) {
      _log.warn("expected one password argument to isValidKey call");
      return false;
    }

    String[] tokens = key.split("\\|");
    if (tokens.length != 2)
      return false;

    String salt = tokens[0];
    String encodedSig = tokens[1];

    byte[] expectedSignature = _coder.decode(encodedSig.getBytes());
    byte[] actualSignature = Digester.digestValue(ALGORITHM, salt.getBytes(),
        arguments[0].getBytes());

    return Arrays.equals(actualSignature, expectedSignature);
  }

  @Override
  public void getKeyInfo(Map<String, String> info, String subKey,
      String... arguments) {
    String value = DigesterSignature.getDecodedValue(subKey);
    info.put("salt", value);
  }

  private String generateRandomSalt(int digits) {
    Random r = new Random();
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < digits; i++) {
      char c = (char) ('a' + r.nextInt(26));
      b.append(c);
    }
    return b.toString();
  }
}