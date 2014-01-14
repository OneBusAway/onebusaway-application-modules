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
package org.onebusaway.transit_data_federation.bundle.tasks;

import java.io.IOException;

import org.onebusaway.container.refresh.RefreshService;
import org.onebusaway.gtfs.impl.calendar.CalendarServiceDataFactoryImpl;
import org.onebusaway.gtfs.impl.calendar.CalendarServiceImpl;
import org.onebusaway.gtfs.model.calendar.CalendarServiceData;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.onebusaway.transit_data_federation.services.FederatedTransitDataBundle;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Pre-computes the {@link CalendarServiceData} that is needed to power a
 * {@link CalendarServiceImpl} and serializes it to disk as part of the transit
 * data bundle.
 * 
 * @author bdferris
 * @see CalendarServiceData
 * @see CalendarServiceImpl
 * @see CalendarService
 * @see CalendarServiceDataFactoryImpl
 */
@Component
public class CalendarServiceDataTask implements Runnable {

  private GtfsRelationalDao _dao;

  private FederatedTransitDataBundle _bundle;

  private RefreshService _refreshService;

  /**
   * By default, we only plan six months in advance
   */
  private int _excludeFutureServiceDatesInDays = 180;

  @Autowired
  public void setGtfsDao(GtfsRelationalDao dao) {
    _dao = dao;
  }

  @Autowired
  public void setBundle(FederatedTransitDataBundle bundle) {
    _bundle = bundle;
  }

  @Autowired
  public void setRefreshService(RefreshService refreshService) {
    _refreshService = refreshService;
  }

  public void setExcludeFutureServiceDatesInDays(
      int excludeFutureServiceDatesInDays) {
    _excludeFutureServiceDatesInDays = excludeFutureServiceDatesInDays;
  }

  public void run() {

    CalendarServiceDataFactoryImpl factory = new CalendarServiceDataFactoryImpl();
    factory.setGtfsDao(_dao);
    factory.setExcludeFutureServiceDatesInDays(_excludeFutureServiceDatesInDays);
    CalendarServiceData data = factory.createData();
    data.makeReadOnly();

    try {

      ObjectSerializationLibrary.writeObject(
          _bundle.getCalendarServiceDataPath(), data);

      _refreshService.refresh(RefreshableResources.CALENDAR_DATA);
    } catch (IOException e) {
      throw new IllegalStateException(
          "error serializing service calendar data", e);
    }
  }
}
