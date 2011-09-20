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
package org.onebusaway.users.impl;

import java.util.ArrayList;
import java.util.List;

public class PhoneNumberLibrary {

  public static String normalizePhoneNumber(String phoneNumber) {
    if (phoneNumber == null)
      return phoneNumber;
    phoneNumber = phoneNumber.replaceAll("[^\\w]", "");
    phoneNumber.replaceAll("_", "");
    if (phoneNumber.length() == 10)
      phoneNumber = "1" + phoneNumber;
    return phoneNumber;
  }

  public static String[] segmentPhoneNumber(String number) {

    number = normalizePhoneNumber(number);

    String callingCode = null;
    String areaCode = null;
    String subscriberNumberA = null;
    String subscriberNumberB = number;

    if (subscriberNumberB != null) {
      int n = number.length();
      if (n > 4) {
        subscriberNumberA = subscriberNumberB.substring(0, n - 4);
        subscriberNumberB = subscriberNumberB.substring(n - 4);
      }
    }

    if (subscriberNumberA != null) {
      int n = subscriberNumberA.length();
      if (n > 3) {
        areaCode = subscriberNumberA.substring(0, n - 3);
        subscriberNumberA = subscriberNumberA.substring(n - 3);
      }
    }

    if (areaCode != null) {
      int n = areaCode.length();
      if (n > 3) {
        callingCode = areaCode.substring(0, n - 3);
        areaCode = areaCode.substring(n - 3);
      }
    }

    List<String> tokens = new ArrayList<String>();

    if (callingCode != null)
      tokens.add(callingCode);
    if (areaCode != null)
      tokens.add(areaCode);
    if (subscriberNumberA != null)
      tokens.add(subscriberNumberA);
    if (subscriberNumberB != null)
      tokens.add(subscriberNumberB);

    return tokens.toArray(new String[tokens.size()]);
  }
}
