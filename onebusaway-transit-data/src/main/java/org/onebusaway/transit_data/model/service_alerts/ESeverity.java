package org.onebusaway.transit_data.model.service_alerts;

public enum ESeverity {

  NO_IMPACT(-2, "noImpact", "pti26_6"),

  /**
   * We make the numeric weight of an undefined or unknown service alert that
   * same as a normal service alert.
   */
  UNDEFINED(-1, "undefined", "pti26_255"),

  UNKNOWN(0, "unknown", "pti26_0"),

  VERY_SLIGHT(2, "verySlight", "pti26_1"),

  SLIGHT(2, "slight", "pti26_2"),

  NORMAL(3, "normal", "pti26_3"),

  SEVERE(4, "severe", "pti26_4"),

  VERY_SEVERE(5, "verySevere", "pti26_5");

  private final int numericValue;

  private final String[] tpegCodes;

  private ESeverity(int numericValue, String... tpegCodes) {
    this.numericValue = numericValue;
    this.tpegCodes = tpegCodes;
  }

  public int getNumericValue() {
    return this.numericValue;
  }

  public String[] getTpegCodes() {
    return tpegCodes;
  }

  public static ESeverity valueOfTpegCode(String value) {
    for (ESeverity severity : values()) {
      for (String code : severity.getTpegCodes()) {
        if (code.equals(value))
          return severity;
      }
    }
    return null;
  }
}
