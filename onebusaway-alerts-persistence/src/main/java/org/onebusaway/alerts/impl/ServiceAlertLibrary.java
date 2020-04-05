/**
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
package org.onebusaway.alerts.impl;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.service_alerts.EEffect;
import org.onebusaway.transit_data.model.service_alerts.ESeverity;
import org.onebusaway.alerts.service.ServiceAlerts.Consequence.Effect;
import org.onebusaway.alerts.service.ServiceAlerts.Id;
import org.onebusaway.alerts.service.ServiceAlerts.ServiceAlert.Severity;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceAlertLibrary {

  private static final Logger _log = LoggerFactory.getLogger(ServiceAlertLibrary.class);

  public static final AgencyAndId agencyAndId(String agencyId, String id) {
    return new AgencyAndId(agencyId, id);
  }

  /**
   * Id may have agencyId duplicated -- do the right thing
   * @return a valid, non duplicated AgencyAndId
   */
  public static final AgencyAndId agencyAndIdAndId(String agencyId, String id) {
    if (id != null && id.indexOf(AgencyAndIdLibrary.ID_SEPARATOR) != -1) {
      return AgencyAndIdLibrary.convertFromString(id);
    }
    return new AgencyAndId(agencyId, id);
  }

  
  public static Id id(AgencyAndId id) {
    return id(id.getAgencyId(), id.getId());
  }

  public static Id id(String agencyId, String id) {
    Id.Builder builder = Id.newBuilder();
    builder.setAgencyId(agencyId);
    builder.setId(id);
    return builder.build();
  }

  public static Severity convertSeverity(ESeverity severity) {
    switch (severity) {
      case NO_IMPACT:
        return Severity.NO_IMPACT;
      case UNKNOWN:
        return Severity.UNKNOWN;
      case VERY_SLIGHT:
        return Severity.VERY_SLIGHT;
      case SLIGHT:
        return Severity.SLIGHT;
      case NORMAL:
        return Severity.NORMAL;
      case SEVERE:
        return Severity.SEVERE;
      case VERY_SEVERE:
        return Severity.VERY_SEVERE;
      default:
        _log.warn("unkown severity level: " + severity);
        return Severity.UNKNOWN;
    }
  }

  public static ESeverity convertSeverity(Severity severity) {
    switch (severity) {
      case NO_IMPACT:
        return ESeverity.NO_IMPACT;
      case UNKNOWN:
        return ESeverity.UNKNOWN;
      case VERY_SLIGHT:
        return ESeverity.VERY_SLIGHT;
      case SLIGHT:
        return ESeverity.SLIGHT;
      case NORMAL:
        return ESeverity.NORMAL;
      case SEVERE:
        return ESeverity.SEVERE;
      case VERY_SEVERE:
        return ESeverity.VERY_SEVERE;
      default:
        _log.warn("unkown severity level: " + severity);
        return ESeverity.UNKNOWN;
    }
  }

  public static EEffect convertEffect(Effect effect) {
    switch (effect) {
      case NO_SERVICE:
        return EEffect.NO_SERVICE;
      case REDUCED_SERVICE:
        return EEffect.REDUCED_SERVICE;
      case SIGNIFICANT_DELAYS:
        return EEffect.SIGNIFICANT_DELAYS;
      case DETOUR:
        return EEffect.DETOUR;
      case ADDITIONAL_SERVICE:
        return EEffect.ADDITIONAL_SERVICE;
      case MODIFIED_SERVICE:
        return EEffect.MODIFIED_SERVICE;
      case OTHER_EFFECT:
        return EEffect.OTHER_EFFECT;
      case UNKNOWN_EFFECT:
        return EEffect.UNKNOWN_EFFECT;
      case STOP_MOVED:
        return EEffect.STOP_MOVED;
      default:
        _log.warn("unknown Consequence.Effect " + effect);
        return EEffect.UNKNOWN_EFFECT;
    }
  }

  public static Effect convertEffect(EEffect effect) {
    switch (effect) {
      case NO_SERVICE:
        return Effect.NO_SERVICE;
      case REDUCED_SERVICE:
        return Effect.REDUCED_SERVICE;
      case SIGNIFICANT_DELAYS:
        return Effect.SIGNIFICANT_DELAYS;
      case DETOUR:
        return Effect.DETOUR;
      case ADDITIONAL_SERVICE:
        return Effect.ADDITIONAL_SERVICE;
      case MODIFIED_SERVICE:
        return Effect.MODIFIED_SERVICE;
      case OTHER_EFFECT:
        return Effect.OTHER_EFFECT;
      case UNKNOWN_EFFECT:
        return Effect.UNKNOWN_EFFECT;
      case STOP_MOVED:
        return Effect.STOP_MOVED;
      default:
        _log.warn("unknown effect " + effect);
        return Effect.UNKNOWN_EFFECT;
    }
  }
}
