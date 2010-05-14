package org.onebusaway.sms.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onebusaway.sms.impl.SessionManagerImpl;

import java.util.Map;

public class SessionManagerImplTest {

  private SessionManagerImpl _sessionManager;

  @Before
  public void setup() {
    _sessionManager = new SessionManagerImpl();
    _sessionManager.setSessionTimeout(10 * 1000);
    _sessionManager.setSessionReapearFrequency(2 * 1000);
    _sessionManager.start();
  }

  @After
  public void teardown() {
    _sessionManager.stop();
  }

  @Test
  public void testSessionManager() {

    Map<String, Object> session = _sessionManager.getContext("A");
    session.put("hello", "world");

    sleep(5 * 1000);

    session = _sessionManager.getContext("A");
    assertEquals("world", session.get("hello"));

    sleep(15 * 1000);

    session = _sessionManager.getContext("A");
    assertFalse(session.containsKey("hello"));
  }

  private static final void sleep(long time) {
    try {
      Thread.sleep(time);
    } catch (InterruptedException e) {
      throw new IllegalStateException(e);
    }
  }
}
