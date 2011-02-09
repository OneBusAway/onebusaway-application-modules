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
