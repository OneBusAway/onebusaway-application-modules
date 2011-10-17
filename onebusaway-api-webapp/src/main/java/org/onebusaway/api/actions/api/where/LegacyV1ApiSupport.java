package org.onebusaway.api.actions.api.where;

public class LegacyV1ApiSupport {

  private static boolean _defaultToV1 = System.getProperties().containsKey(
      LegacyV1ApiSupport.class.getName());

  public static boolean isDefaultToV1() {
    return _defaultToV1;
  }
}
