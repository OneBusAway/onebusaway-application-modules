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
package org.onebusaway.phone.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.onebusaway.presentation.services.text.TextModification;

public class LocationPronunciation implements TextModification {

  private static final Pattern _pattern = Pattern.compile("^(.*)\\b(\\d{5})$");

  private TextModification _additionalPronunciation;

  public void setAdditionalPronunciation(
      TextModification additionalPronunciation) {
    _additionalPronunciation = additionalPronunciation;
  }

  public String modify(String input) {

    Matcher m = _pattern.matcher(input);
    if (m.matches()) {
      String rest = m.group(1);
      String zip = m.group(2);

      StringBuilder b = new StringBuilder();
      for (int i = 0; i < zip.length(); i++) {
        if (i > 0)
          b.append(" ");
        b.append(zip.charAt(i));
      }
      input = rest + b.toString();
    }

    if (_additionalPronunciation != null)
      input = _additionalPronunciation.modify(input);

    return input;
  }

}
