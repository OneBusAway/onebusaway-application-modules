package org.onebusaway.utility.text;

public class StringLibrary {

  public static String getBestName(String... names) {
    for (String name : names) {
      if (name == null)
        continue;
      name = name.trim();
      if (name.length() > 0)
        return name;
    }
    return null;
  }

}
