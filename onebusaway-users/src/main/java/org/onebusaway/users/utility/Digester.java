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
