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
package org.onebusaway.api.actions.siri;

import org.onebusaway.presentation.impl.service_alerts.ServiceAlertsHelper;
import org.onebusaway.presentation.services.realtime.RealtimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import uk.org.siri.siri.Siri;

/**
 * Base action for Siri Monitoring V1 calls.
 */
public class MonitoringActionV1Base extends SiriAction {

  private static Logger _log = LoggerFactory.getLogger(MonitoringActionV1Base.class);

  // note this is a v1 helper
  protected ServiceAlertsHelper _serviceAlertsHelper = new ServiceAlertsHelper();
  // note this is v1 siri
  protected Siri _siriResponse;
  @Autowired
  protected RealtimeService _realtimeService;

  public MonitoringActionV1Base(int defaultVersion) {
    super(defaultVersion);
  }

  public String getSiri() {
    // we no longer set response types here -- custom handlers take care of that
    // based on the type param
    try {
      if(getType().equals("xml")) {
        return _realtimeService.getSiriXmlSerializer().getXml(_siriResponse);
      } else {
        return _realtimeService.getSiriJsonSerializer().getJson(_siriResponse,
                /*_servletRequest.getParameter("callback")*/null);
        // callback happens at a lower level
      }
    } catch(Exception e) {
      _log.error("Siri v1 serialization failed: ", e,e);
      return e.toString();
    }
  }

}
