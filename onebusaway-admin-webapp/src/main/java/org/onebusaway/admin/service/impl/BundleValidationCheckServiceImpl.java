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
package org.onebusaway.admin.service.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.onebusaway.admin.model.ParsedBundleValidationCheck;
import org.onebusaway.admin.service.BundleValidationCheckService;

public abstract class BundleValidationCheckServiceImpl implements
    BundleValidationCheckService {

  protected static final String ARRIVALS_AND_DEPARTURES_FOR_STOP =
      "/where/arrivals-and-departures-for-stop/";
  protected static final String DATE_FLD = "date=";
  protected static final String ROUTE = "/where/route/";
  protected static final String ROUTES_FOR_AGENCY = "/where/routes-for-agency/";
  protected static final String SCHEDULE_FOR_STOP = "/where/schedule-for-stop/";
  protected static final String STOP = "/where/stop/";
  protected static final String STOP_MONITORING = "/stop-monitoring?";

  @Override
  public abstract String buildQuery(String envURI, String apiKey,
      String apiQuery, String siriQuery,
      ParsedBundleValidationCheck check);
  
  protected static String getNextDayOfWeek(int dayOfWeek) {
    // Get next specified day of week
    Calendar c = Calendar.getInstance();
    c.set(Calendar.DAY_OF_WEEK, dayOfWeek);
    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    return df.format(c.getTime());
  }
}
