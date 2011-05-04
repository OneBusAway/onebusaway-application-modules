package org.onebusaway.api.services;

import org.onebusaway.transit_data.model.RegisterAlarmQueryBean;

public interface AlarmService {

  public AlarmDetails alterAlarmQuery(RegisterAlarmQueryBean alarm, String data);

  public void registerAlarm(String alarmId, AlarmDetails details);
  
  public void fireAlarm(String alarmId);
  
  public void cancelAlarm(String alarmId);
}
