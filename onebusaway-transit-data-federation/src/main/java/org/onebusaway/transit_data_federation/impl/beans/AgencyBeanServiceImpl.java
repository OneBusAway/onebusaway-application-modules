/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
 * Copyright (C) 2015 University of South Florida (cagricetin@mail.usf.edu)
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
package org.onebusaway.transit_data_federation.impl.beans;

import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data_federation.model.narrative.AgencyNarrative;
import org.onebusaway.transit_data_federation.services.beans.AgencyBeanService;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class AgencyBeanServiceImpl implements AgencyBeanService {

  private NarrativeService _narrativeService;

  @Autowired
  public void setNarrativeService(NarrativeService narrativeService) {
    _narrativeService = narrativeService;
  }

  @Cacheable
  public AgencyBean getAgencyForId(String id) {

    AgencyNarrative agency = _narrativeService.getAgencyForId(id);

    if (agency == null)
      return null;

    AgencyBean bean = new AgencyBean();
    bean.setId(id);
    bean.setLang(agency.getLang());
    bean.setName(agency.getName());
    bean.setPhone(agency.getPhone());
    bean.setEmail(agency.getEmail());
    bean.setTimezone(agency.getTimezone());
    bean.setUrl(agency.getUrl());
    bean.setDisclaimer(agency.getDisclaimer());
    bean.setPrivateService(agency.isPrivateService());
    bean.setFareUrl(agency.getFareUrl());
    bean.setEmail(agency.getEmail());

    return bean;
  }
}
