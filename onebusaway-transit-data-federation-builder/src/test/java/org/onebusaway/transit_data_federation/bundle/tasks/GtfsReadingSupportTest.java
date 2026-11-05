/**
 * Copyright (C) 2026 OneBusAway
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
package org.onebusaway.transit_data_federation.bundle.tasks;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Test;

public class GtfsReadingSupportTest {

  @Test
  public void testSha256Hex() throws Exception {
    File file = File.createTempFile("gtfs-hash-test", ".txt");
    try {
      Files.write(file.toPath(), "onebusaway-test".getBytes(StandardCharsets.UTF_8));
      // expected value verified with: printf 'onebusaway-test' | sha256sum
      assertEquals("10d100b36f08b76029c21495b141ef2472041a61368fbc3a8687701bd8210d74",
          GtfsReadingSupport.sha256Hex(file));
    } finally {
      file.delete();
    }
  }
}
