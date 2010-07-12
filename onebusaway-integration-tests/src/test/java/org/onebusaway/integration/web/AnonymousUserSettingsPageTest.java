package org.onebusaway.integration.web;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AnonymousUserSettingsPageTest extends WebTestSupport {

  @Test
  public void test() {

    open("/logout.action");
    open("/user/index.action");

    assertTrue(isTextPresent("User Info"));
    assertTrue(isTextPresent("Current Settings:Preferences ARE saved"));
    assertTrue(isTextPresent("Your Default Search Location:Not Set"));
    assertTrue(isTextPresent("You can register your account, or login to an existing account."));
  }
}
