package org.onebusaway.integration.phone;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class WelcomeTest extends PhoneTestSupport {

  @Test
  public void test() {

    assertEquals("ANSWER", getReplyAsText());
    assertEquals("WAIT FOR DIGIT 1000", getReplyAsText());

    assertEquals(sayAlpha("Where is your bus... lets find out.  "
        + "If you are a first time user, or need help, please press zero."),
        getReplyAsText());
    assertEquals("WAIT FOR DIGIT 500", getReplyAsText());

    // Make sure it repeats
    for (int i = 0; i < 2; i++) {
      assertEquals(sayAlpha("To enter a stop number, please press one."),
          getReplyAsText());
      assertEquals(
          sayAlpha("For help finding your stop number, please press two."),
          getReplyAsText());
      assertEquals(
          sayAlpha("To access your bookmarked stops, please press three."),
          getReplyAsText());
      assertEquals(
          sayAlpha("To manage your bookmarked stops, please press four."),
          getReplyAsText());
      assertEquals(
          sayAlpha("To check your most recent stop, please press five."),
          getReplyAsText());
      assertEquals(
          sayAlpha("To search for a stop by route number, please press six."),
          getReplyAsText());
      assertEquals(
          sayAlpha("To adjust your personal settings, please press seven."),
          getReplyAsText());
      assertEquals(sayAlpha("If you need help, please press zero."),
          getReplyAsText());
      assertEquals(sayAlpha("to repeat"), getReplyAsText());
    }
  }
}
