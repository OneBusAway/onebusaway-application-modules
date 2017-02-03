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
package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern;

import java.util.Arrays;

public class ArraySearchTestMain {
  public static void main(String[] args) {

    short max = 13000;
    short[] values = new short[max * 3];

    for (short i = 0; i < max; i++) {
      int offset = i * 3;
      values[offset] = i;
      values[offset + 1] = i;
      values[offset + 2] = i;
    }

    System.out.println(Arrays.toString(values));

    long tIn = System.currentTimeMillis();

    for (int i = 0; i < 10000; i++) {
      short v = (short) (Math.random() * max);
      Arrays.binarySearch(values, v);
    }

    long tOut = System.currentTimeMillis();

    System.out.println(tOut - tIn);
  }
}
