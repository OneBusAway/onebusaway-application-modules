package org.onebusaway.gtfs_realtime.archiver.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

@Component
public class TimeServiceImpl implements TimeService {
  
  private static final int SESSIONS_THRESHOLD = 1000;
  
  // Session is not cleared automatically right now.
  Map<String, Session> sessions;
  
  @PostConstruct
  public void init() {
    sessions = new HashMap<String, Session>(); 
  }
  
  @Override
  public Date getCurrentTime(String session) {
    Session s = sessions.get(session);
    if (s == null) {
      return null;
    }
        
    long time = s.originalTime.getTime() + (new Date().getTime() - s.timeSet.getTime());
    return new Date(time);
  }

  @Override
  public void setCurrentTime(String session, Date time) {
    
    if (sessions.size() > SESSIONS_THRESHOLD) {
      sessions.clear();
    }
    
    Session s = new Session();
    s.originalTime = time;
    s.timeSet = new Date();
    sessions.put(session, s);
  }

  @Override
  public boolean isTimeSet(String session) {
    Session s = sessions.get(session);
    return s != null && s.originalTime != null;
  }

  @Override
  public void clear(String session) {
    sessions.remove(session);
  }
  
  private class Session {
    Date originalTime;
    Date timeSet;
  }
}
