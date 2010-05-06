package org.onebusaway.integration.phone;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Before;

public class PhoneTestSupport extends PhoneClient {

  public static final String RESET_USER = "agi_oba_integration_test_reset_user";

  private Future<?> _task;

  public PhoneTestSupport() {
    setHost("localhost");
    setPort(8001);
    setCallerId("2065551234");
    setNetworkScript("index.agi");
    setResetUser(true);
  }

  public void setResetUser(boolean resetUser) {
    if (resetUser)
      setParameter(RESET_USER, "true");
    else
      removeParameter(RESET_USER);
  }

  @Before
  public void start() throws IOException {
    _task = run();
  }

  @After
  public void stop() {
    _task.cancel(true);
  }

  public String sayAlpha(String message) {
    return "SAY ALPHA \"" + message + "\" \"0123456789#*\"";
  }

  public void sendLongResponse(String response) {
    sendResponse(response);
    for (int i = 0; i < response.length() - 1; i++)
      assertEquals("WAIT FOR DIGIT 5000", getReplyAsText(false));
  }

  public boolean waitForText(String match, int maxSteps) {
    for (int i = 0; i < maxSteps; i++) {
      String text = getReplyAsText(false);
      if (text.contains(match))
        return true;
      sendDefaultResponse();
    }
    return false;
  }
}
