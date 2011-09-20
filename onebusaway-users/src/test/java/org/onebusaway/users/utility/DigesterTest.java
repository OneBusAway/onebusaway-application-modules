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

import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

public class DigesterTest {

  @Test
  public void testSHA1() throws UnsupportedEncodingException {
    byte[] salt = "abc".getBytes();
    byte[] input = "def".getBytes();
    byte[] message = Digester.digestValue("SHA1", salt, input);
    assertEquals("1f8ac10f23c5b5bc1167bda84b833e5c057a77d2", new String(
        Hex.encodeHex(message)));
  }

  @Test
  public void testSHA256() throws UnsupportedEncodingException {
    byte[] salt = "abc".getBytes();
    byte[] input = "def".getBytes();
    byte[] message = Digester.digestValue("SHA-256", salt, input);
    char[] hex = Hex.encodeHex(message);
    assertEquals(
        "bef57ec7f53a6d40beb640a780a639c83bc29ac8a9816f1fc6c5c6dcd93c4721",
        new String(hex));
  }
}
