package org.onebusaway.sms.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManagerImpl {

  private ConcurrentHashMap<String, ContextEntry> _contextEntriesByKey = new ConcurrentHashMap<String, ContextEntry>();

  private long _sessionReaperFrequency = 60 * 1000;

  private long _sessionTimeout = 10 * 60 * 1000;

  private SessionCleanup _sessionCleanup;

  private Thread _thread;

  private boolean _exit = false;

  public void setSessionReapearFrequency(long sessionReaperFrequency) {
    _sessionReaperFrequency = sessionReaperFrequency;
  }

  public void setSessionTimeout(long sessionTimeout) {
    _sessionTimeout = sessionTimeout;
  }

  public void start() {
    _sessionCleanup = new SessionCleanup();
    _thread = new Thread(_sessionCleanup);
    _thread.start();
  }

  public void stop() {

    try {
      setExit();
      _thread.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    _contextEntriesByKey.clear();
  }

  public Map<String, Object> getContext(String key) {
    ContextEntry entry = getOrCreateContextEntry(key);
    return entry.getContext();
  }

  private ContextEntry getOrCreateContextEntry(String key) {
    while (true) {
      ContextEntry entry = new ContextEntry();
      ContextEntry existingEntry = _contextEntriesByKey.putIfAbsent(key, entry);
      entry = (existingEntry == null) ? entry : existingEntry;
      if (entry.isValidAfterTouch())
        return entry;
    }
  }

  private void setExit() {
    synchronized (_sessionCleanup) {
      _exit = true;
      _sessionCleanup.notify();
    }
  }

  private static class ContextEntry {

    private long _lastAccess;

    private Map<String, Object> _context = new HashMap<String, Object>();

    private boolean _valid = true;

    public synchronized boolean isValidAfterTouch() {
      if (!_valid)
        return false;
      _lastAccess = System.currentTimeMillis();
      return true;
    }

    public synchronized boolean isValidAfterAccessCheck(long minTime) {
      if (_lastAccess < minTime)
        _valid = false;
      return _valid;
    }

    public Map<String, Object> getContext() {
      return _context;
    }

  }

  private class SessionCleanup implements Runnable {

    public void run() {

      while (true) {

        synchronized (this) {

          if (_exit)
            return;

          try {
            wait(_sessionReaperFrequency);
            if (_exit)
              return;
          } catch (InterruptedException e) {
            e.printStackTrace();
            return;
          }
        }

        long minTime = System.currentTimeMillis() - _sessionTimeout;

        Iterator<ContextEntry> it = _contextEntriesByKey.values().iterator();

        while (it.hasNext()) {
          ContextEntry entry = it.next();
          if (!entry.isValidAfterAccessCheck(minTime))
            it.remove();
        }
      }

    }

  }
}
