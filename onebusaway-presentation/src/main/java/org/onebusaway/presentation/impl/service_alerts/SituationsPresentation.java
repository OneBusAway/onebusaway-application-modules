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
package org.onebusaway.presentation.impl.service_alerts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.onebusaway.collections.CollectionsLibrary;
import org.onebusaway.transit_data.model.service_alerts.ESeverity;
import org.onebusaway.transit_data.model.service_alerts.NaturalLanguageStringBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectedApplicationBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectsBean;
import org.onebusaway.transit_data.model.service_alerts.SituationBean;
import org.onebusaway.transit_data.model.service_alerts.SituationConsequenceBean;
import org.onebusaway.transit_data.model.service_alerts.TimeRangeBean;
import org.onebusaway.users.client.model.UserBean;

public class SituationsPresentation {

  private static RecentSituationComparator _comparator = new RecentSituationComparator();

  private List<SituationBean> _situations;

  private UserBean _user;

  private long _time = System.currentTimeMillis();

  private long _redisplayIfNowActiveThhreshold = 24 * 60 * 60 * 1000;

  private String _apiKey = "web";

  public void setApiKey(String apiKey) {
    _apiKey = apiKey;
  }

  public String getApiKey() {
    return _apiKey;
  }

  public void setSituations(List<SituationBean> situations) {
    _situations = determineApplicationSituations(situations);
  }

  public void setUser(UserBean user) {
    _user = user;
  }

  public void setRedisplayIfNowActiveThreshold(
      long redisplayIfNowActiveThhreshold) {
    _redisplayIfNowActiveThhreshold = redisplayIfNowActiveThhreshold;
  }

  public int getTotalCount() {
    return _situations.size();
  }

  public int getUnreadCount() {

    int unreadServiceAlertCount = 0;
    Map<String, Long> readServiceAlerts = _user.getReadServiceAlerts();
    for (SituationBean situation : _situations) {

      if (isSituationUnread(readServiceAlerts, situation))
        unreadServiceAlertCount++;
    }
    return unreadServiceAlertCount;
  }

  public String getUnreadServiceAlertsClass() {
    ESeverity severity = getHighestUnreadSeverity();
    if (severity == ESeverity.NO_IMPACT)
      return "unreadServiceAlertsNoImpactSeverity";
    return "unreadServiceAlertsNormalSeverity";
  }

  public List<SituationBean> getUnreadSituations() {
    List<SituationBean> situations = new ArrayList<SituationBean>();
    Map<String, Long> readServiceAlerts = _user.getReadServiceAlerts();
    for (SituationBean situation : _situations) {
      if (isSituationUnread(readServiceAlerts, situation))
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
    if (isSet(situation.getSummary()))
      return getValue(situation.getSummary());
    if (isSet(situation.getDescription()))
      return getValue(situation.getDescription());
    return null;
  }

  /****
   * Private Methods
   ****/

  public boolean isSet(NaturalLanguageStringBean bean) {
    return bean != null && bean.getValue() != null
        && !bean.getValue().trim().isEmpty();
  }

  private String getValue(NaturalLanguageStringBean nls) {
    String value = nls.getValue();
    return value;
  }

  private boolean isSituationUnread(Map<String, Long> readServiceAlerts,
      SituationBean situation) {

    if (!isSituationInActivePublicationWindow(situation))
      return false;

    Long readTime = readServiceAlerts.get(situation.getId());

    if (readTime == null)
      return true;

    /**
     * Optionally, if a service alert has just become active and it hasn't been
     * read in a while, we might also consider it unread.
     */
    if (readTime + _redisplayIfNowActiveThhreshold < _time) {
      if (!isSituationActiveAtTime(situation, readTime)
          && isSituationActiveAtTime(situation, _time))
        return true;
    }

    return false;
  }

  private boolean isSituationInActivePublicationWindow(SituationBean situation) {
    TimeRangeBean window = situation.getPublicationWindow();
    return isTimeRangeActive(window, _time, true);
  }

  private boolean isSituationActiveAtTime(SituationBean situation, long time) {
    if (situation.getConsequences() == null)
      return true;
    for (SituationConsequenceBean consequences : situation.getConsequences()) {
      TimeRangeBean period = consequences.getPeriod();
      if (isTimeRangeActive(period, time, false))
        return false;
    }
    return true;
  }

  private boolean isTimeRangeActive(TimeRangeBean window, long time,
      boolean defaultOnUnspecified) {

    /**
     * If no publication window is specified, we assume it's always active
     */
    if (window == null)
      return defaultOnUnspecified;

    long from = window.getFrom();
    long to = window.getTo();

    /**
     * Again, if no publication window is specified, we assume it's always
     * active
     */
    if (from == 0 && to == 0)
      return defaultOnUnspecified;

    if (from == 0 && _time <= to)
      return true;

    if (to == 0 && from <= _time)
      return true;

    return (from <= _time && _time <= to);
  }

  private ESeverity getHighestUnreadSeverity() {

    ESeverity maxSeverity = null;

    Map<String, Long> readServiceAlerts = _user.getReadServiceAlerts();
    for (SituationBean situation : _situations) {

      if (isSituationUnread(readServiceAlerts, situation)) {
        ESeverity s = situation.getSeverity();
        if (s == null)
          s = ESeverity.UNDEFINED;
        if (maxSeverity == null
            || maxSeverity.getNumericValue() < s.getNumericValue())
          maxSeverity = s;
      }
    }
    return maxSeverity;
  }

  private List<SituationBean> determineApplicationSituations(
      List<SituationBean> situations) {
    List<SituationBean> applicable = new ArrayList<SituationBean>();
    for (SituationBean situation : situations) {
      if (isSituationApplicable(situation))
        applicable.add(situation);
    }
    return applicable;
  }

  private boolean isSituationApplicable(SituationBean situation) {
    SituationAffectsBean affects = situation.getAffects();
    if (affects == null)
      return true;
    List<SituationAffectedApplicationBean> applications = affects.getApplications();
    if (CollectionsLibrary.isEmpty(applications))
      return true;
    if (_apiKey == null)
      return false;
    for (SituationAffectedApplicationBean application : applications) {
      if (_apiKey.equals(application.getApiKey()))
        return true;
    }
    return false;
  }
}
