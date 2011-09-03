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
package org.onebusaway.transit_data.model.service_alerts;

public enum ESensitivity {

  VERY_LOW(-2, "veryLow"),

  LOW(-1, "low"),

  MEDIUM(0, "medium"),

  HIGH(1, "high"),

  VERY_HIGH(2, "veryHigh");

  private final int numericValue;

  private final String xmlValue;

  private ESensitivity(int numericValue, String xmlValue) {
    this.numericValue = numericValue;
    this.xmlValue = xmlValue;
  }

  public int getNumericValue() {
    return this.numericValue;
  }

  public String getXmlValue() {
    return xmlValue;
  }

  public static ESensitivity valueOfXmlId(String value) {
    for (ESensitivity sensitivty : values()) {
      if (value.equals(sensitivty.getXmlValue()))
        return sensitivty;
    }
    return null;
  }
}
