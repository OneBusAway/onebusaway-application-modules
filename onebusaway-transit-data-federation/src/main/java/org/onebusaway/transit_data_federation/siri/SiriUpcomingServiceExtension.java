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
package org.onebusaway.transit_data_federation.siri;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType(name = "ScheduledService", propOrder = {
      "upcomingScheduledService"
})
public class SiriUpcomingServiceExtension {

  @XmlElement(name = "UpcomingScheduledService")
  protected Boolean upcomingScheduledService = null;

  public Boolean hasUpcomingScheduledService() {
        return upcomingScheduledService;
    }

    public void setUpcomingScheduledService(Boolean value) {
        this.upcomingScheduledService = value;
    }
}
