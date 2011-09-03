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
    _sessionManager.setSessionTimeout(10);
    _sessionManager.setSessionReapearFrequency(2);
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
