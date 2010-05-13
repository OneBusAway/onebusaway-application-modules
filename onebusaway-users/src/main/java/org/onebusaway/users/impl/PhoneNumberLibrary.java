package org.onebusaway.users.impl;

public class PhoneNumberLibrary {

  public static String normalizePhoneNumber(String phoneNumber) {
    if (phoneNumber == null)
      return phoneNumber;
    if (phoneNumber.startsWith("+"))
      phoneNumber = phoneNumber.substring(1);
    if (phoneNumber.length() == 10)
      phoneNumber = "1" + phoneNumber;
    return phoneNumber;
  }

}
