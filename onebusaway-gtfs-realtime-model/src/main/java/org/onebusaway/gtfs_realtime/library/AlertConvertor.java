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
package org.onebusaway.gtfs_realtime.library;

import java.util.Date;

import org.onebusaway.gtfs_realtime.model.AlertModel;
import org.onebusaway.gtfs_realtime.model.EntitySelectorModel;
import org.onebusaway.gtfs_realtime.model.TimeRangeModel;

import com.google.transit.realtime.GtfsRealtime.Alert;
import com.google.transit.realtime.GtfsRealtime.EntitySelector;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.TimeRange;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;

public class AlertConvertor extends FeedEntityConvertor<AlertModel> {

  @Override
  public AlertModel readFeedEntity(FeedEntity entity, long timestamp) {
    if (entity.hasAlert()) {
      AlertModel alrt = new AlertModel();
      alrt.setTimestamp(new Date(timestamp));
      Alert alert = entity.getAlert();
      for (TimeRange tr : alert.getActivePeriodList()) {
        TimeRangeModel timeRange = readTimeRange(tr);
        if (timeRange != null) {
          timeRange.setAlert(alrt);
          alrt.addTimeRangeModel(timeRange);
        }
      }
      for (EntitySelector es : alert.getInformedEntityList()) {
        EntitySelectorModel entitySelector = readEntitySelector(es);
        if (entitySelector != null) {
          entitySelector.setAlert(alrt);
          alrt.addEntitySelectorModel(entitySelector);
        }
      }
      if (alert.hasCause()) {
        String cause = alert.getCause().getValueDescriptor().getFullName();
        cause = cause.substring(cause.lastIndexOf('.') + 1);
        alrt.setCause(cause);
      }
      if (alert.hasEffect()) {
        String effect = alert.getEffect().getValueDescriptor().getFullName();
        effect = effect.substring(effect.lastIndexOf('.') + 1);
        alrt.setEffect(effect);
      }
      if (alert.hasUrl()) {
        alrt.setUrl(alert.getUrl().getTranslation(0).getText());
      }
      if (alert.hasHeaderText()) {
        alrt.setHeaderText(alert.getHeaderText().getTranslation(0).getText());
      }
      if (alert.hasDescriptionText()) {
        alrt.setDescriptionText(
            alert.getDescriptionText().getTranslation(0).getText());
      }
      return alrt;
    }
    return null;
  }

  private static TimeRangeModel readTimeRange(TimeRange tr) {
    if (tr == null)
      return null;
    TimeRangeModel trm = new TimeRangeModel();
    if (tr.hasStart()) {
      trm.setStart(tr.getStart());
    }
    if (tr.hasEnd()) {
      trm.setEnd(tr.getEnd());
    }
    return trm;
  }

  private static EntitySelectorModel readEntitySelector(EntitySelector es) {
    if (es == null)
      return null;
    EntitySelectorModel esm = new EntitySelectorModel();
    if (es.hasAgencyId()) {
      esm.setAgencyId(es.getAgencyId());
    }
    if (es.hasRouteId()) {
      esm.setRouteId(es.getRouteId());
    }
    if (es.hasRouteType()) {
      esm.setRouteType(es.getRouteType());
    }
    if (es.hasStopId()) {
      esm.setStopId(es.getStopId());
    }
    if (es.hasTrip()) {
      TripDescriptor t = es.getTrip();
      if (t.hasTripId())
        esm.setTripId(t.getTripId());
      if (t.hasRouteId()) {
        esm.setRouteId(t.getRouteId());
      }
      if (t.hasStartTime()) {
        esm.setTripStartTime(t.getStartTime());
      }
      if (t.hasStartDate()) {
        esm.setTripStartDate(t.getStartDate());
      }
    }
    return esm;
  }

}
