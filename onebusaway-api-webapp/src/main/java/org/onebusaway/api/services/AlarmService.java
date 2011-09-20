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
package org.onebusaway.api.services;

import org.onebusaway.transit_data.model.RegisterAlarmQueryBean;

public interface AlarmService {

  public AlarmDetails alterAlarmQuery(RegisterAlarmQueryBean alarm, String data);

  public void registerAlarm(String alarmId, AlarmDetails details);
  
  public void fireAlarm(String alarmId);
  
  public void cancelAlarm(String alarmId);
}
