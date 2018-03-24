/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data_federation.bundle.tasks.stif;

public enum StifTripType {
  PULLOUT, PULLIN, DEADHEAD, REVENUE;

  public static StifTripType byValue(int value) {
    switch (value) {
      case 1: case 11: case 12: case 13: case 14:
        return REVENUE;
      case 2:
        return PULLOUT;
      case 3:
        return PULLIN;
      case 4:
        return DEADHEAD;
      default:
        return null;
    }
  }

}
