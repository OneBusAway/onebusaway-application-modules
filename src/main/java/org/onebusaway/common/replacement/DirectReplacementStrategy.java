package org.onebusaway.common.replacement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DirectReplacementStrategy implements IReplacementStrategy {

  private Map<String, String> _replacements = new HashMap<String, String>();

  public void setReplacementsFromFile(File file) throws IOException {

    BufferedReader reader = new BufferedReader(new FileReader(file));
    String line = null;

    while ((line = reader.readLine()) != null) {
      int index = line.indexOf("=");
      if (index != -1) {
        String key = line.substring(0, index);
        String value = line.substring(index + 1);
        _replacements.put(key, value);
      }
    }

    reader.close();
  }

  public String replace(String input) {
    if (_replacements.containsKey(input))
      return _replacements.get(input);
    return input;
  }

}
