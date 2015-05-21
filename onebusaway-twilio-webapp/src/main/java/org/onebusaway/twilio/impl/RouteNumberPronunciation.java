package org.onebusaway.twilio.impl;

import org.onebusaway.presentation.services.text.TextModification;

public class RouteNumberPronunciation implements TextModification {

  public String modify(String input) {

    boolean express = false;

    if (input.endsWith("E")) {
      express = true;
      input = input.replaceAll("E$", "");
    } else if (input.endsWith("X")) {
      express = true;
      input = input.replaceAll("X$", "");
    }

    int n = input.length();
    if (n > 2)
      input = input.substring(0, n - 2) + " " + input.substring(n - 2);

    if (express)
      input += " express";

    return input;
  }

}
