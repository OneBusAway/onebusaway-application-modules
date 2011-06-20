package org.onebusaway.sms.impl;

import org.onebusaway.presentation.impl.ArrivalsAndDeparturesModel;
import org.onebusaway.presentation.services.text.TextModification;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
public class SmsArrivalsAndDeparturesModel extends ArrivalsAndDeparturesModel {

  private TextModification _abbreviations;
  
  @Autowired
  public void setDestinationAbbreviations(
      @Qualifier("smsDestinationAbbreviations") TextModification strategy) {
    _abbreviations = strategy;
  }

  public String getMinutesLabel(ArrivalAndDepartureBean pab) {
    long now = System.currentTimeMillis();
    long t = pab.getScheduledDepartureTime();
    if (pab.hasPredictedDepartureTime())
      t = pab.getPredictedDepartureTime();
    int minutes = (int) Math.round((t - now) / (1000.0 * 60.0));
    boolean isNow = Math.abs(minutes) <= 1;
    return isNow ? "NOW" : (Integer.toString(minutes) + "m");
  }

  public String abbreviate(String destination) {
    return _abbreviations.modify(destination);
  }
}
