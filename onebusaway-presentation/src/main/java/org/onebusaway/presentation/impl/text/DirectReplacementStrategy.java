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
package org.onebusaway.presentation.impl.text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.onebusaway.presentation.services.text.TextModification;

public class DirectReplacementStrategy implements TextModification {

  private Map<String, String> _replacements = new HashMap<String, String>();

  public void setReplacements(Map<String, String> replacements) {
    _replacements.putAll(replacements);
  }

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

  public String modify(String input) {
    if (_replacements.containsKey(input))
      return _replacements.get(input);
    return input;
  }

}
