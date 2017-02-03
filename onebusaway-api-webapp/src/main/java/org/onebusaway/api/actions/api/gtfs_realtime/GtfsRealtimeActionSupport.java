/**
 * Copyright (C) 2013 Google, Inc.
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
package org.onebusaway.api.actions.api.gtfs_realtime;

import java.util.Date;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.transit.realtime.GtfsRealtime.FeedHeader;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtimeConstants;
import com.opensymphony.xwork2.conversion.annotations.TypeConversion;
import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;

public abstract class GtfsRealtimeActionSupport extends ApiActionSupport {

  private static final long serialVersionUID = 1L;

  private static final int V2 = 2;

  @Autowired
  protected TransitDataService _service;

  private String _agencyId;

  private long _time;

  private boolean _removeAgencyIds = true;

  public GtfsRealtimeActionSupport() {
    super(V2);
  }
  
  public void setTransitDataService(TransitDataService service) {
    _service = service;
  }

  @RequiredFieldValidator
  public void setId(String id) {
    _agencyId = id;
  }

  public String getId() {
    return _agencyId;
  }

  @TypeConversion(converter = "org.onebusaway.presentation.impl.conversion.DateTimeConverter")
  public void setTime(Date time) {
    _time = time.getTime();
  }

  public void setRemoveAgencyIds(boolean removeAgencyIds) {
    _removeAgencyIds = removeAgencyIds;
  }

  public DefaultHttpHeaders show() throws ServiceException {
    if (!isVersion(V2))
      return setUnknownVersionResponse();

    if (hasErrors())
      return setValidationErrorsResponse();

    long time = System.currentTimeMillis();
    if (_time != 0)
      time = _time;

    FeedMessage.Builder feed = FeedMessage.newBuilder();
    FeedHeader.Builder header = feed.getHeaderBuilder();
    header.setGtfsRealtimeVersion(GtfsRealtimeConstants.VERSION);
    header.setTimestamp(time / 1000);
    fillFeedMessage(feed, _agencyId, time);
    return setOkResponse(feed.build());
  }

  protected abstract void fillFeedMessage(FeedMessage.Builder feed,
      String agencyId, long timestamp);

  protected String normalizeId(String id) {
    if (_removeAgencyIds) {
      int index = id.indexOf('_');
      if (index != -1) {
        id = id.substring(index + 1);
      }
    }
    return id;
  }
}
