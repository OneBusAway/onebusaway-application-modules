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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.onebusaway.twilio.services.SessionManager;
import org.onebusaway.util.SystemTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SessionManagerImpl implements SessionManager {
  private static Logger _log = LoggerFactory.getLogger(SessionManagerImpl.class);

  private ConcurrentHashMap<String, ContextEntry> _contextEntriesByKey = new ConcurrentHashMap<String, ContextEntry>();

  private ScheduledExecutorService _executor;

  private int _sessionReaperFrequency = 60;

  private int _sessionTimeout = 7 * 60;

  /**
   * The frequency with which we'll check for stale sessions
   * 
   * @param sessionReaperFrequency time, in seconds
   */
  public void setSessionReapearFrequency(int sessionReaperFrequency) {
  	_log.debug("setSessionReapearFrequency");
    _sessionReaperFrequency = sessionReaperFrequency;
  }

  /**
   * Timeout, in seconds, at which point a session will be considered stale
   * 
   * @param sessionTimeout time, in seconds
   */
  public void setSessionTimeout(int sessionTimeout) {
  	_log.debug("setSessionTimeout");
  	_sessionTimeout = sessionTimeout;
  }

  @PostConstruct
  public void start() {
	_log.debug("start");
    _executor = Executors.newSingleThreadScheduledExecutor();
    _executor.scheduleAtFixedRate(new SessionCleanup(),
        _sessionReaperFrequency, _sessionReaperFrequency, TimeUnit.SECONDS);
  }

  @PreDestroy
  public void stop() {
  	_log.debug("stop");
    _executor.shutdownNow();
  }

  /****
   * {@link SessionManager} Interface
   ****/

  @Override
  public Map<String, Object> getContext(String key) {
  	_log.debug("getContext");
    ContextEntry entry = getOrCreateContextEntry(key);
    return entry.getContext();
  }
  
  @Override
  public boolean hasContext(String key) {
  	_log.debug("hasContext");
    return _contextEntriesByKey.containsKey(key);
  }

  /****
   * Private Method
   ****/

  private ContextEntry getOrCreateContextEntry(String key) {
    while (true) {
      ContextEntry entry = new ContextEntry();
      ContextEntry existingEntry = _contextEntriesByKey.putIfAbsent(key, entry);
      entry = (existingEntry == null) ? entry : existingEntry;
      if (!entry.getContext().containsKey("twilioCreationTime")) {
        entry.getContext().put("twilioCreationTime", SystemTime.currentTimeMillis());
      }
      if (entry.isValidAfterTouch())
        return entry;
    }
  }

  private static class ContextEntry {

    private long _lastAccess;

    private Map<String, Object> _context = new HashMap<String, Object>();

    private boolean _valid = true;

    public synchronized boolean isValidAfterTouch() {
      //_log.debug("isValidAfterTouch");
      if (!_valid)
        return false;
      _lastAccess = SystemTime.currentTimeMillis();
      return true;
    }

    public synchronized boolean isValidAfterAccessCheck(long minTime) {
      //_log.debug("isValidAfterAccessCheck");
      if (_lastAccess < minTime)
        _valid = false;
      return _valid;
    }

    public Map<String, Object> getContext() {
      _log.debug("getContext");
      return _context;
    }

  }

  private class SessionCleanup implements Runnable {

    public void run() {
      //_log.debug("run");

      long minTime = SystemTime.currentTimeMillis() - _sessionTimeout * 1000;

      Iterator<ContextEntry> it = _contextEntriesByKey.values().iterator();

      while (it.hasNext()) {
        ContextEntry entry = it.next();
        if (!entry.isValidAfterAccessCheck(minTime))
          it.remove();
      }
    }
  }
}
