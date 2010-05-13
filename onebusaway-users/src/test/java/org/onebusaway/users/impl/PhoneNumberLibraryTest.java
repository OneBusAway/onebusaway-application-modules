package org.onebusaway.users.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PhoneNumberLibraryTest {

  @Test
  public void test() {

    assertEquals("12065551234",
        PhoneNumberLibrary.normalizePhoneNumber("12065551234"));

    assertEquals("12065551234",
        PhoneNumberLibrary.normalizePhoneNumber("+12065551234"));

    assertEquals("12065551234",
        PhoneNumberLibrary.normalizePhoneNumber("2065551234"));

    assertEquals("12065551234",
        PhoneNumberLibrary.normalizePhoneNumber("+2065551234"));

    assertEquals("5551234", PhoneNumberLibrary.normalizePhoneNumber("5551234"));
  }
}
