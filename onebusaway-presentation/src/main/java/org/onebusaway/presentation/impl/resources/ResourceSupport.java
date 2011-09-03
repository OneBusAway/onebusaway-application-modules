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
package org.onebusaway.presentation.impl.resources;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class ResourceSupport {

  public static String getHash(InputStream in) throws IOException {

    MessageDigest digester = getDigester();
    byte[] buffer = new byte[1024];

    while (true) {
      int rc = in.read(buffer);
      if (rc == -1)
        break;
      digester.update(buffer, 0, rc);
    }

    return getDistestAsKey(digester);
  }

  public static String getHash(String value) {
    MessageDigest digester = getDigester();
    digester.update(value.getBytes());
    return getDistestAsKey(digester);
  }

  /*****************************************************************************
   * Private Methods
   ****************************************************************************/

  private static MessageDigest getDigester() {
    MessageDigest digester = null;

    try {
      digester = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      System.exit(-1);
    }
    return digester;
  }

  private static String getDistestAsKey(MessageDigest digester) {

    byte[] digest = digester.digest();

    StringBuilder sb = new StringBuilder();

    for (byte b : digest) {
      String hex = Integer.toHexString((int) b & 0xff);
      if (hex.length() == 1)
        sb.append('0');
      sb.append(hex);
    }

    return sb.toString();
  }

}
