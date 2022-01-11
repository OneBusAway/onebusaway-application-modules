/**
 * Copyright (c) 2018 the original author or authors.
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

import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.codec.Utf8;
import org.springframework.util.Assert;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This is a port of Spring Core's original MessageDigestEncoder to maintain
 * backwards compatibility.  It is not considered secure!
 * It maintains the original license header which by coincidence is the same license
 * as this project.
 */
public class LegacyMessageDigestPasswordEncoder extends LegacyBaseDigestPasswordEncoder {
  private final String algorithm;
  private int iterations;

  public LegacyMessageDigestPasswordEncoder(String algorithm) {
    this(algorithm, false);
  }

  public LegacyMessageDigestPasswordEncoder(String algorithm, boolean encodeHashAsBase64) throws IllegalArgumentException {
    this.iterations = 1;
    this.algorithm = algorithm;
    this.setEncodeHashAsBase64(encodeHashAsBase64);
    this.getMessageDigest();
  }

  public String encodePassword(String rawPass, Object salt) {
    String saltedPass = this.mergePasswordAndSalt(rawPass, salt, false);
    MessageDigest messageDigest = this.getMessageDigest();
    byte[] digest = messageDigest.digest(Utf8.encode(saltedPass));

    for (int i = 1; i < this.iterations; ++i) {
      digest = messageDigest.digest(digest);
    }

    return this.getEncodeHashAsBase64() ? Utf8.decode(Base64.encode(digest)) : new String(Hex.encode(digest));
  }

  protected final MessageDigest getMessageDigest() throws IllegalArgumentException {
    try {
      return MessageDigest.getInstance(this.algorithm);
    } catch (NoSuchAlgorithmException var2) {
      throw new IllegalArgumentException("No such algorithm [" + this.algorithm + "]");
    }
  }

  public boolean isPasswordValid(String encPass, String rawPass, Object salt) {
    String pass1 = "" + encPass;
    String pass2 = this.encodePassword(rawPass, salt);
    return PasswordEncoderUtils.equals(pass1, pass2);
  }

  public String getAlgorithm() {
    return this.algorithm;
  }

  public void setIterations(int iterations) {
    Assert.isTrue(iterations > 0, "Iterations value must be greater than zero");
    this.iterations = iterations;
  }

  @Override
  // format of {salt}password to stay compatible!
  public String encode(CharSequence charSequence) {
    String salt = extractSalt((String) charSequence);
    String password = (String) charSequence;
    if (salt != null && salt.length() > 2)
      password = ((String) charSequence).substring(salt.length()+2);
    return encodePassword(password, salt);
  }

  @Override
  public boolean matches(CharSequence charSequence, String s) {
    return false;
  }
  private String extractSalt(String prefixEncodedPassword) {
    String delimitedSalt = null;
    int start = prefixEncodedPassword.indexOf("{");
    if (start != 0) {
      return "";
    } else {
      int end = prefixEncodedPassword.indexOf("}", start);
      delimitedSalt = end < 0 ? "" : prefixEncodedPassword.substring(start, end + 1);
    }
    if (delimitedSalt != null & delimitedSalt.length() > 1) {
      delimitedSalt = delimitedSalt.substring(1, delimitedSalt.length()-1);
      return delimitedSalt;
    }
    return "";
  }

  protected String mergePasswordAndSalt(String password, Object salt, boolean strict) {
    if (password == null) {
      password = "";
    }

    if (strict && salt != null && (salt.toString().lastIndexOf("{") != -1 || salt.toString().lastIndexOf("}") != -1)) {
      throw new IllegalArgumentException("Cannot use { or } in salt.toString()");
    } else {
      return salt != null && !"".equals(salt) ? password + "{" + salt.toString() + "}" : password;
    }
  }

  static class PasswordEncoderUtils {
    static boolean equals(String expected, String actual) {
      byte[] expectedBytes = bytesUtf8(expected);
      byte[] actualBytes = bytesUtf8(actual);
      int expectedLength = expectedBytes == null ? -1 : expectedBytes.length;
      int actualLength = actualBytes == null ? -1 : actualBytes.length;
      int result = expectedLength == actualLength ? 0 : 1;

      for (int i = 0; i < actualLength; ++i) {
        byte expectedByte = expectedLength <= 0 ? 0 : expectedBytes[i % expectedLength];
        byte actualByte = actualBytes[i % actualLength];
        result |= expectedByte ^ actualByte;
      }

      return result == 0;
    }

    private static byte[] bytesUtf8(String s) {
      return s == null ? null : Utf8.encode(s);
    }

    private PasswordEncoderUtils() {
    }

  }
}
