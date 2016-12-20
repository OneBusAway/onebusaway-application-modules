/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.collections.CollectionsLibrary;
import org.onebusaway.transit_data.model.service_alerts.ESeverity;
import org.onebusaway.transit_data.model.service_alerts.NaturalLanguageStringBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectsBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.service_alerts.TimeRangeBean;
import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.util.SystemTime;

public class SituationsPresentation {

  private static RecentSituationComparator _comparator = new RecentSituationComparator();

  private List<ServiceAlertBean> _situations;

  private UserBean _user;

  private long _time = SystemTime.currentTimeMillis();

  private long _redisplayIfNowActiveThhreshold = 24 * 60 * 60 * 1000;

  private String _apiKey = "web";

  public void setApiKey(String apiKey) {
    _apiKey = apiKey;
  }

  public String getApiKey() {
    return _apiKey;
  }

  public void setSituations(List<ServiceAlertBean> situations) {
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
    for (ServiceAlertBean situation : _situations) {

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

  public List<ServiceAlertBean> getUnreadSituations() {
    List<ServiceAlertBean> situations = new ArrayList<ServiceAlertBean>();
    Map<String, Long> readServiceAlerts = _user.getReadServiceAlerts();
    for (ServiceAlertBean situation : _situations) {
      if (isSituationUnread(readServiceAlerts, situation))
        situations.add(situation);
    }
    Collections.sort(situations, _comparator);
    return situations;
  }

  public List<ServiceAlertBean> getReadSituations() {
    List<ServiceAlertBean> situations = new ArrayList<ServiceAlertBean>();
    Map<String, Long> readServiceAlerts = _user.getReadServiceAlerts();
    for (ServiceAlertBean situation : _situations) {
      if (readServiceAlerts.containsKey(situation.getId()))
        situations.add(situation);
    }
    Collections.sort(situations, _comparator);
    return situations;
  }

  public String getTitle(ServiceAlertBean situation) {
    /**
     * TODO: Better handling around language selection
     */
    if (!CollectionsLibrary.isEmpty(situation.getSummaries()))
      return getValue(situation.getSummaries().get(0));
    if (!CollectionsLibrary.isEmpty(situation.getDescriptions()))
      return getValue(situation.getDescriptions().get(0));
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
      ServiceAlertBean situation) {

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

  private boolean isSituationInActivePublicationWindow(ServiceAlertBean situation) {
    if (CollectionsLibrary.isEmpty(situation.getPublicationWindows()))
      return true;
    for (TimeRangeBean window : situation.getPublicationWindows()) {
      if (isTimeRangeActive(window, _time, true))
        return true;
    }
    return false;
  }

  private boolean isSituationActiveAtTime(ServiceAlertBean situation, long time) {
    if (CollectionsLibrary.isEmpty(situation.getActiveWindows()))
      return true;
    for (TimeRangeBean window : situation.getActiveWindows()) {
      if (isTimeRangeActive(window, _time, true))
        return true;
    }
    return false;
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
    for (ServiceAlertBean situation : _situations) {

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

  private List<ServiceAlertBean> determineApplicationSituations(
      List<ServiceAlertBean> situations) {
    List<ServiceAlertBean> applicable = new ArrayList<ServiceAlertBean>();
    for (ServiceAlertBean situation : situations) {
      if (isSituationApplicable(situation))
        applicable.add(situation);
    }
    return applicable;
  }

  private boolean isSituationApplicable(ServiceAlertBean situation) {
    Set<String> applicationIds = new HashSet<String>();
    for (SituationAffectsBean affects : situation.getAllAffects()) {
      if (affects.getApplicationId() != null)
        applicationIds.add(affects.getApplicationId());
    }
    if (CollectionsLibrary.isEmpty(applicationIds))
      return true;
    if (_apiKey == null)
      return false;
    return applicationIds.contains(_apiKey);
  }
}
