/**
 * Copyright (C) 2014 HART (Hillsborough Area Regional Transit) 
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
