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

import org.onebusaway.container.ConfigurationParameter;
import org.onebusaway.presentation.impl.ArrivalsAndDeparturesModel;
import org.onebusaway.presentation.services.text.TextModification;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.util.SystemTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
public class SmsArrivalsAndDeparturesModel extends ArrivalsAndDeparturesModel {

  private static final String DEFAULT_MINUTE_LOCALIZATION = "m";
  private TextModification _abbreviations;
  private String _minuteLocalization = DEFAULT_MINUTE_LOCALIZATION;
  private String alertPresentText = "";
  
  @Autowired(required=false)
  public void setDestinationAbbreviations(
      @Qualifier("smsDestinationAbbreviations") TextModification strategy) {
    _abbreviations = strategy;
  }

  @ConfigurationParameter
  public void setMinuteLocalization(String abbrev) {
    _minuteLocalization = abbrev;
  }

  public void setAlertPresentText(String msg) {
    alertPresentText = msg;
  }

  public String getAlertPresentText() { return alertPresentText; }


  public String getMinutesLabel(ArrivalAndDepartureBean pab) {
    long now = SystemTime.currentTimeMillis();
    long t = pab.getScheduledDepartureTime();
    if (pab.hasPredictedDepartureTime())
      t = pab.getPredictedDepartureTime();
    int minutes = (int) Math.round((t - now) / (1000.0 * 60.0));
    boolean isNow = Math.abs(minutes) <= 1;
    return isNow ? "NOW" : (Integer.toString(minutes) + _minuteLocalization);
  }

  public String abbreviate(String destination) {
    if( _abbreviations == null)
      return destination;
    return _abbreviations.modify(destination);
  }
}
