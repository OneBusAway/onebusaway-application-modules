/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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
package org.onebusaway.gtfs_realtime.archiver.service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.google.common.cache.CacheBuilder;

@Component
public class TimeServiceImpl implements TimeService {
  
  Map<String, Session> sessions;
  
  @PostConstruct
  public void init() {
    sessions = CacheBuilder.newBuilder()
      .expireAfterAccess(1, TimeUnit.DAYS)
      .<String, Session>build()
      .asMap();
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
    Session s = sessions.get(session);
    if (s != null && time.equals(s.originalTime)) {
      return;
    }
    
    s = new Session();
    s.originalTime = time;
    s.timeSet = new Date();
    sessions.put(session, s);
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
