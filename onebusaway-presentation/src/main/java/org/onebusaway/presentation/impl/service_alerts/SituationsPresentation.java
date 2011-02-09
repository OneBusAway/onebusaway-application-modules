package org.onebusaway.presentation.impl.service_alerts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.onebusaway.transit_data.model.service_alerts.NaturalLanguageStringBean;
import org.onebusaway.transit_data.model.service_alerts.SituationBean;
import org.onebusaway.users.client.model.UserBean;

public class SituationsPresentation {

  private static RecentSituationComparator _comparator = new RecentSituationComparator();

  private List<SituationBean> _situations;

  private UserBean _user;

  public void setSituations(List<SituationBean> situations) {
    _situations = situations;
  }

  public void setUser(UserBean user) {
    _user = user;
  }

  public int getTotalCount() {
    return _situations.size();
  }

  public int getUnreadCount() {
    int unreadServiceAlertCount = 0;
    Map<String, Long> readServiceAlerts = _user.getReadServiceAlerts();
    for (SituationBean situation : _situations) {
      if (!readServiceAlerts.containsKey(situation.getId()))
        unreadServiceAlertCount++;
    }
    return unreadServiceAlertCount;
  }

  public List<SituationBean> getUnreadSituations() {
    List<SituationBean> situations = new ArrayList<SituationBean>();
    Map<String, Long> readServiceAlerts = _user.getReadServiceAlerts();
    for (SituationBean situation : _situations) {
      if (!readServiceAlerts.containsKey(situation.getId()))
        situations.add(situation);
    }
    Collections.sort(situations, _comparator);
    return situations;
  }

  public List<SituationBean> getReadSituations() {
    List<SituationBean> situations = new ArrayList<SituationBean>();
    Map<String, Long> readServiceAlerts = _user.getReadServiceAlerts();
    for (SituationBean situation : _situations) {
      if (readServiceAlerts.containsKey(situation.getId()))
        situations.add(situation);
    }
    Collections.sort(situations, _comparator);
    return situations;
  }
  
  public String getTitle(SituationBean situation) {
    if( isSet(situation.getSummary()))
      return getValue(situation.getSummary());
    if( isSet(situation.getDescription()))
      return getValue(situation.getDescription());
    return null;
  }
  

  /****
   * Private Methods
   ****/
  
  public boolean isSet(NaturalLanguageStringBean bean) {
    return bean != null && bean.getValue() != null && ! bean.getValue().trim().isEmpty();
  }
  
  private String getValue(NaturalLanguageStringBean nls) {
    String value = nls.getValue();
    return value;
  }

}
