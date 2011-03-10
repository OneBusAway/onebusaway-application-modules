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
